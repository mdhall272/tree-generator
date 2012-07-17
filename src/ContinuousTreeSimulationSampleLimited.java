import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 16/01/2012
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
public class ContinuousTreeSimulationSampleLimited extends ContinuousTreeSimulation {

    private int finalNumberOfSamples;
    private boolean okToStop;
    private double whenToStop;
    private double timeSpentWithMaxSamples;
    private double timeOfLastSample;
    private double currentDeepestTipHeight;
    private ArrayList<NodeAndHeight> heightLookup;

    public ContinuousTreeSimulationSampleLimited(double samplingProbability, double samplingStartTime,
                                                 int finalNumberOfSamples, double R0, double incubation_k,
                                                 double incubation_theta, double infectious_k,
                                                 double infectious_theta, boolean recordIncubationPeriods){
        super(samplingProbability, samplingStartTime, R0, incubation_k, infectious_k, incubation_theta,
                infectious_theta, recordIncubationPeriods);
        this.finalNumberOfSamples=finalNumberOfSamples;
        currentDeepestTipHeight=0;
        nodeCount=0;
        heightLookup=new ArrayList<NodeAndHeight>();
        this.recordInfectiousPeriods = recordIncubationPeriods;
    }

    public void simulateInfection(ForwardRootedNode transmissionNode) {
        Branch branch = new Branch(incubation_k, incubation_theta, infectious_k, infectious_theta, R0);
        double[] branchWaits = branch.getBranchWaits();
        ForwardRootedNode currentNode = transmissionNode;
        boolean sampled = false;
        double timeTillDeath = arraySum(branchWaits);

        //Determine if the dog was sampled after death
        if(tree.getHeight(transmissionNode)+timeTillDeath>=samplingStartTime) {
            sampled = (drawIfSampled());
        }
        String ID = "";
        if(sampled){
            ID="S" + sampledCorpseCounter;
            sampledCorpseCounter++;
        } else {
            ID="N" + unsampledCorpseCounter;
            unsampledCorpseCounter++;
        }

        if(recordInfectiousPeriods){
            hostInfectiousPeriods.put(ID,branch.getInfectiousPeriod());
        }

        boolean outOfTime = false;
        int i=0;
        while(!outOfTime && i<branchWaits.length){
            //What to do if the current child node occurs after sampling (and thus the simulation) has finished.
            if(okToStop && tree.getHeight(currentNode)+branchWaits[i]>whenToStop){
                ForwardRootedNode finalNode = placeExternalChild(tree, currentNode,
                        whenToStop - tree.getHeight(currentNode), false);
                try {
                    setEdgeNumber(tree.getEdge(finalNode,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                tree.setTaxon(finalNode, "CL" + continuingBranches + " " + nodeCount);
                finalNode.setAttribute("Node number", nodeCount);
                finalNode.setAttribute("Sampled", false);
                heightLookup.add(new NodeAndHeight(finalNode, tree.getHeight(finalNode)));
                continuingBranches++;
                outOfTime=true;
                nodeCount++;
            }
            //What to do if the current child node represents a transmission
            else if(i<branchWaits.length-1){
                ForwardRootedNode currentChild = placeInternalChild(tree,currentNode,branchWaits[i]);
                try {
                    setEdgeNumber(tree.getEdge(currentChild,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                currentChild.setAttribute("Infector", ID);
                currentChild.setAttribute("Node number", nodeCount);
                nodeCount++;
                simulateInfection(currentChild);
                currentNode=currentChild;
                i++;
            }
            //What to do if the current child node represents the death of the dog
            else {
                ForwardRootedNode deadDog = placeExternalChild(tree,currentNode,branchWaits[branchWaits.length-1],
                        sampled);
                try {
                    setEdgeNumber(tree.getEdge(deadDog,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                if(sampled){
                    updateSampling(deadDog);
                }
                if(deadDog.getHeight()>currentDeepestTipHeight) {
                    currentDeepestTipHeight=deadDog.getHeight();
                }
                if(sampledCorpseCounter>=finalNumberOfSamples+1 && !okToStop){
                    okToStop=true;
                    whenToStop=currentDeepestTipHeight;
                }
                tree.setTaxon(deadDog,ID + " " + nodeCount);
                deadDog.setAttribute("Node number", nodeCount);
                deadDog.setAttribute("Sampled", sampled);
                heightLookup.add(new NodeAndHeight(deadDog, tree.getHeight(deadDog)));
                nodeCount++;
                i++;
            }
        }
    }

    public double getTimeSpentWithMaxSamples() {
        return timeSpentWithMaxSamples;
    }

    private ForwardRootedTree pruneToLimit (ForwardRootedTree tree) {
        Collections.sort(heightLookup, new CompareNAHs());
        int chronologicalSampledCorpseCounter=1;
        int chronologicalUnsampledCorpseCounter=1;
        boolean stillSampling=true;
        for (int i=0 ; i<heightLookup.size(); i++){
            ForwardRootedNode node = heightLookup.get(i).getNode();
            if((Boolean)node.getAttribute("Sampled")){
                tree.setTaxon(node, "S" + chronologicalSampledCorpseCounter);
                chronologicalSampledCorpseCounter++;
            } else {
                tree.setTaxon(node, "N" + chronologicalUnsampledCorpseCounter);
                chronologicalUnsampledCorpseCounter++;
            }
            if(chronologicalSampledCorpseCounter==finalNumberOfSamples+2 && stillSampling){
                stillSampling=false;
                timeSpentWithMaxSamples=heightLookup.get(i).getHeight()-heightLookup.get(i-1).getHeight();
                timeOfLastSample=heightLookup.get(i-1).getHeight();
            }
        }

        /*If the process ended without ever exceeding the set maximum number of samples, need to set
       timeOfLastSample to include the entire tree*/

        if(stillSampling){
            timeOfLastSample=Collections.max(heightLookup, new CompareNAHs()).getHeight();
            timeSpentWithMaxSamples=Double.POSITIVE_INFINITY;
        }

        TrimToTimeLimit tttl = new TrimToTimeLimit(timeOfLastSample);
        return tttl.trim(tree);
    }

    private boolean checkForRetainedChildren(ForwardRootedNode node){
        List<Node> children = tree.getChildren(node);
        for(Node currentNode: children){
            if(tree.isExternal(currentNode) && tree.getHeight(currentNode)<=timeOfLastSample){
                return true;
            }
            if(!tree.isExternal(currentNode)){
                if(checkForRetainedChildren((ForwardRootedNode)currentNode)){
                    return true;
                }
            }

        }
        return false;
    }


    public int[] runSimulation (boolean runSamplesOnly, String basicFileName, String prunedFileName,
                                String originalFileName, String networkOutputFileName, String dataTableOutputFile,
                                double reportToCull, double estimateJitter){

        int[] nodeNumbers = new int[3];

        /*Place the root and get it. This root represents the transmission that started the tree and does not result in
        a bifurcation. Done like this since its length is actually informative.*/
        ForwardRootedNode rootNode = placeRoot(false);
        //Simulate all the infection
        rootNode.setAttribute("Node number", nodeCount);
        nodeCount++;
        simulateInfection(rootNode);
        nodeNumbers[0]=tree.getExternalNodes().size();
        System.out.println("Nodes in original tree: " + nodeNumbers[0]);
        WriteToFile.write(tree, originalFileName);
        ForwardRootedTree trimmedTree = pruneToLimit(tree);
        nodeNumbers[1]=trimmedTree.getExternalNodes().size();
        System.out.println("Nodes in trimmed tree: " + nodeNumbers[1]);


        //Write to file

        WriteToFile.write(trimmedTree, basicFileName);

        ForwardRootedTree prunedTree = trimmedTree;

        /*For purposes of generating trees for network reconstruction with 100% sampling, you need to do this to
        remove continuing lineages*/

        if (runSamplesOnly){
            RemoveUnsampled RUNs = new RemoveUnsampled();
            try {
                prunedTree = RUNs.transform(prunedTree);
                nodeNumbers[2]=prunedTree.getExternalNodes().size();
                System.out.println("Nodes in samples-only tree: " + nodeNumbers[2]);
                WriteToFile.write(prunedTree, prunedFileName);
            }
            catch (NoSamplesException e) {
                nodeNumbers[2]=0;
                System.out.println("Nodes in samples-only tree: " + nodeNumbers[2]);
            }
        }

        TreeToNetwork.outputCSVNetwork(networkOutputFileName, prunedTree);
        if(recordInfectiousPeriods){
            TreeToDataTable.outputCSVDataTable(dataTableOutputFile, prunedTree, reportToCull, estimateJitter,
                    hostInfectiousPeriods);
        }

        return nodeNumbers;
    }

    public ForwardRootedTree runSimulationNoOutput(){
        int[] nodeNumbers = new int[3];
        ForwardRootedNode rootNode = placeRoot(false);
        rootNode.setAttribute("Node number", nodeCount);
        nodeCount++;
        simulateInfection(rootNode);
        nodeNumbers[0]=tree.getExternalNodes().size();
        System.out.print("O:" + nodeNumbers[0] + " ");
        ForwardRootedTree trimmedTree = pruneToLimit(tree);
        nodeNumbers[1]=trimmedTree.getExternalNodes().size();
        System.out.print("T:" + nodeNumbers[1] + " ");
        RemoveUnsampled RUNs = new RemoveUnsampled();
        try {
            ForwardRootedTree prunedTree = RUNs.transform(trimmedTree);
            nodeNumbers[2]=prunedTree.getExternalNodes().size();
            System.out.print("S:" + nodeNumbers[2] + " ");
            return prunedTree;
        }
        catch (NoSamplesException e) {
            nodeNumbers[2]=0;
            System.out.print("S:" + nodeNumbers[2] + " ");
        }
        return null;
    }

    public static void main (String [] args){
        ContinuousTreeSimulationSampleLimited rabiesSim =
                new ContinuousTreeSimulationSampleLimited(1, 0, 50, 1.5, 1, 1, 1, 1, true);
        rabiesSim.runSimulation(true, "allfarms1.nex",
                "allfarms2.nex",
                "contlineages.nex",
                "network.csv",
                "datatable.csv", 2, 0);
    }

}






