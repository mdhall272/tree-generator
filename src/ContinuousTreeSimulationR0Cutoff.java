import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.math.Random;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileWriter;
import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 17/07/2012
 * Time: 09:40
 * To change this template use File | Settings | File Templates.
 */
public class ContinuousTreeSimulationR0Cutoff extends ContinuousTreeSimulation {

    private double cutoff;

    // In this version, R0 drops to 0 after a certain time. Sampling never stops.

    public ContinuousTreeSimulationR0Cutoff(double samplingProbability, double samplingStartTime,
                                            double cutoff, double R0, double incubation_k,
                                            double incubation_theta, double infectious_k,
                                            double infectious_theta, boolean recordIncubationPeriods){
        super(samplingProbability,samplingStartTime,R0,incubation_k,incubation_theta,infectious_k,infectious_theta,
                recordIncubationPeriods);
        this.cutoff=cutoff;
    }

    public void simulateInfection(ForwardRootedNode transmissionNode, double currentR0, String unsampledPrefix,
                                  String sampledPrefix) {
        Branch branch = new TruncNormalGammaBranch(incubation_mean, incubation_stdev, infectious_k, infectious_theta,
                currentR0);
        double[] branchWaits = branch.getBranchWaits();
        ForwardRootedNode currentNode = transmissionNode;
        boolean sampled = false;
        double timeTillDeath = arraySum(branchWaits);

        //Determine if the dog was sampled after death
        if(tree.getHeight(transmissionNode)+timeTillDeath>=samplingStartTime) {
            sampled = (drawIfSampled());
        }
        String ID;
        if(sampled){
            ID=sampledPrefix + sampledCorpseCounter;
            sampledCorpseCounter++;
        } else {
            ID=unsampledPrefix + unsampledCorpseCounter;
            unsampledCorpseCounter++;
        }

        if(recordInfectiousPeriods){
            hostInfectiousPeriods.put(ID,branch.getInfectiousPeriod());
        }

        int i=0;
        while(i<branchWaits.length){
            //What to do if the current child node represents a transmission
            if(i<branchWaits.length-1){
                ForwardRootedNode currentChild = placeInternalChild(tree,currentNode,branchWaits[i]);
                try {
                    setEdgeNumber(tree.getEdge(currentChild,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                currentChild.setAttribute("Infector", ID);
                currentChild.setAttribute("Node number", nodeCount);
                nodeCount++;
                simulateInfection(currentChild, currentChild.getHeight()<cutoff ? R0 : 0, unsampledPrefix,
                        sampledPrefix);
                currentNode=currentChild;
                i++;
            }
            //What to do if the current child node represents the death of the dog
            else {
                ForwardRootedNode deadDog = placeExternalChild(tree, currentNode, branchWaits[branchWaits.length - 1],
                        sampled);
                try {
                    setEdgeNumber(tree.getEdge(deadDog,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                if(sampled){
                    updateSampling(deadDog);
                }
                tree.setTaxon(deadDog,ID);
                deadDog.setAttribute("Node number", nodeCount);
                deadDog.setAttribute("Sampled", sampled);
                nodeCount++;
                i++;
            }
        }
    }

    public boolean runSimulation (boolean runSamplesOnly, boolean produceOMDinput, String basicFileName,
                                  String prunedFileName, String omdInputFileName, String networkOutputFileName,
                                  String dataTableOutputFile, double reportToCull, double estimateJitter,
                                  int minTipsForOutput, int maxTipsForOutput, String farmIDPrefix, DateTime startDate){

        int[] nodeNumbers = new int[2];

        /*Place the root and get it. This root represents the transmission that started the tree and does not result in
        a bifurcation. Done like this since its length is actually informative.*/
        ForwardRootedNode rootNode = placeRoot(false);
        //Simulate all the infection
        rootNode.setAttribute("Node number", nodeCount);
        nodeCount++;
        simulateInfection(rootNode, R0, farmIDPrefix+"U", farmIDPrefix+"S");
        nodeNumbers[0]=tree.getExternalNodes().size();
        System.out.println("Nodes in original tree: " + nodeNumbers[0]);
        if(tree.getExternalNodes().size()>=minTipsForOutput && tree.getExternalNodes().size()<=maxTipsForOutput){
            System.out.println("Writing to file: "+basicFileName);

            WriteToNexusFile.write(tree, basicFileName);

            if(produceOMDinput){
                ForwardRootedTree newTree = new ForwardRootedTree(tree);
                ForwardRootedNode root = (ForwardRootedNode)newTree.getRootNode();
                ForwardRootedNode rootChild = root.getChildren().get(0);
                rootChild.setParent(null);
                newTree.removeNode(root);
                WriteToNewickFile.write(newTree, omdInputFileName);
            }

            ForwardRootedTree prunedTree = tree;

            /*For purposes of generating trees for network reconstruction with 100% sampling, you need to do this to
          remove continuing lineages*/

            if (runSamplesOnly){
                RemoveUnsampled RUNs = new RemoveUnsampled();
                try {
                    prunedTree = RUNs.transform(prunedTree);
                    nodeNumbers[1]=prunedTree.getExternalNodes().size();
                    System.out.println("Nodes in samples-only tree: " + nodeNumbers[1]);
                    WriteToNexusFile.write(prunedTree, prunedFileName);
                }
                catch (NoSamplesException e) {
                    nodeNumbers[1]=0;
                    System.out.println("Nodes in samples-only tree: " + nodeNumbers[1]);
                }
            }

            TreeToNetwork.outputCSVNetwork(networkOutputFileName, prunedTree);
            if(recordInfectiousPeriods){
                TreeToDataTable.outputCSVDataTable(dataTableOutputFile, prunedTree, reportToCull, estimateJitter,
                        startDate, hostInfectiousPeriods);
            }
            return true;
        } else {
            System.out.println("Skipping");
            return false;
        }

    }

    public static void runSimulations(double samplingProbability, double samplingStartTime, double cutoff, double R0,
                                      double incubation_mean, double incubation_stdev, double infectious_k,
                                      double infectious_theta, boolean recordIncubationPeriods, boolean runSamplesOnly,
                                      boolean produceOMDinput, String basicFileNameRoot, String prunedFileNameRoot,
                                      String networkOutputFileNameRoot, String dataTableOutputFileRoot,
                                      double reportToCull, double estimateJitter, int minTips, int maxTips,
                                      String startDateString, int desiredRuns, int firstRunNo){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        DateTime startDate = formatter.parseDateTime(startDateString);

        int run = 1;
        while(run<=desiredRuns){
            String extension = "_"+(firstRunNo-1+run);
            ContinuousTreeSimulationR0Cutoff thisSim = new ContinuousTreeSimulationR0Cutoff(samplingProbability,
                    samplingStartTime, cutoff, R0, incubation_mean, incubation_stdev, infectious_k, infectious_theta,
                    recordIncubationPeriods);
            boolean keep = thisSim.runSimulation(runSamplesOnly, produceOMDinput, basicFileNameRoot+extension+".nex",
                    prunedFileNameRoot+extension+".nex", basicFileNameRoot+extension+".newick",
                    networkOutputFileNameRoot+extension+".csv", dataTableOutputFileRoot+extension+".csv", reportToCull,
                    estimateJitter, minTips, maxTips, "Run"+extension+"_Farm_", startDate);
            if(keep){
                run++;
            }
        }

    }

    public static void main (String [] args){
        try {
            int noDifferentRuns = 10;
            int noSimilarRuns = 1;
            FileWriter writer = new FileWriter("incubation_period_parameters.txt");
            int totalCount = 1;
            for (int i=1; i<=noDifferentRuns; i++){
                double random_mean = Random.nextDouble()*9 + 1;
                double random_stdev = Random.nextDouble()*5;
                writer.write  ("Simulation " + i + ": Mean " + random_mean + ", SD " +
                        random_stdev + "\n");
                writer.flush();
                runSimulations(1, 0, 30, 1.5, random_mean, random_stdev, 2, 2, true, false, true, "allfarms",
                        "contlineages", "network", "datatable", 0, 1, 40, 60, "01/01/2012", noSimilarRuns,
                        totalCount);
                totalCount = totalCount + noSimilarRuns;
            }
        }catch(IOException e){
            System.out.println("IOException");
        }
    }
}
