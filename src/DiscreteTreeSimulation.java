import jebl.evolution.graphs.Graph;
import jebl.evolution.io.NexusExporter;
import jebl.math.Random;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 01/12/2011
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
public class DiscreteTreeSimulation extends TreeSimulation {

    private DrawPoisson fish;
    private double samplingEndTime;

    public DiscreteTreeSimulation(double samplingProbability, double samplingStartTime, double samplingEndTime, double R0){
        super(samplingProbability, samplingStartTime, R0);
        this.R0=R0;
        fish = new DrawPoisson(R0);
        this.samplingEndTime=samplingEndTime;
    }


    public void simulateInfection(ForwardRootedNode splitNode) {
        // Start with a transmission point - a node with at least two children
        double randomNumber = Random.nextDouble();
        DrawPoisson fish = new DrawPoisson(R0);
        int numberOfChildren = fish.draw(randomNumber);
        boolean sampled = false;
        if(tree.getHeight(splitNode)>=samplingStartTime && tree.getHeight(splitNode)<=samplingEndTime) {
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
        splitNode.setAttribute("Infector", ID);
        for(int i=0;i<numberOfChildren+1;i++) {
            //First, create the node corresponding to the death of the dog.
            if(i==0){
                ForwardRootedNode deadDog = placeExternalChild(tree,splitNode,0,sampled);
                try {
                    setEdgeNumber(tree.getEdge(deadDog,splitNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                if(sampled){
                    updateSampling(deadDog);
                }
                tree.setTaxon(deadDog,ID);
            } else if(tree.getHeight(splitNode)+1>samplingEndTime){
                ForwardRootedNode finalNode = placeExternalChild(tree,splitNode,1,false);
                try {
                    setEdgeNumber(tree.getEdge(finalNode,splitNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                tree.setTaxon(finalNode,"CL"+continuingBranches);
                continuingBranches++;
            } else {
                ForwardRootedNode currentChild = placeInternalChild(tree,splitNode,1);
                DiscreteTreeSimulation.changeSampledDeath(currentChild, false);
                try {
                    setEdgeNumber(tree.getEdge(currentChild,splitNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                simulateInfection(currentChild);
            }
        }
    }

    /*Main method. To be GUIfied*/

    public static void main (String[] args){
        DiscreteTreeSimulation rabiesSim = new DiscreteTreeSimulation(0.5, 0, 20, 1.2);
        /*Place the root and get it. The root has only one child, since the length of this branch is important (it 
        *might later get combined with a subsequent branch*/
        ForwardRootedNode rootNode = rabiesSim.placeRoot(false,false);
        ForwardRootedNode secondNode = placeInternalChild(rabiesSim.tree,rootNode, 1);
        try {
            setEdgeNumber(rabiesSim.tree.getEdge(rootNode,secondNode),0);
        } catch(Graph.NoEdgeException e) {
            System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
        }
        changeSampledDeath(rootNode, false);
        changeSampledDeath(secondNode, false);
        //Simulate all the infection
        rabiesSim.simulateInfection(secondNode);
        System.out.println("Total number of external nodes: " + rabiesSim.tree.getExternalNodes().size());

        //Write to file

        try {PrintWriter writer = new PrintWriter(new FileWriter("outputtree.tre"));
            NexusExporter nexp = new NexusExporter(writer);
            nexp.exportTree(rabiesSim.tree);
            writer.flush();
        }
        catch (IOException e) {
            System.out.println("File problem");
        }

        //Strip out the unsampled nodes and any internal nodes that would not be there if their presence was undetected

        DiscreteRemoveUnsampled RUNs = new DiscreteRemoveUnsampled();
        try {
            ForwardRootedTree prunedTree = RUNs.transform(rabiesSim.tree, true);
            System.out.println("Total number of sampled external nodes: " + prunedTree.getExternalNodes().size());

            //Write the second tree to file

            try {PrintWriter writer2 = new PrintWriter(new FileWriter("outputprunedtree.tre"));
                NexusExporter nexp2 = new NexusExporter(writer2);
                nexp2.exportTree(prunedTree);
                writer2.flush();
            }
            catch (IOException e) {
                System.out.println("File problem");
            }
        }
        catch (NoSamplesException e) {
            System.out.println("Total number of sampled external nodes: 0");
        }


    }

    /*Change the flag to determine whether this node represents a transmission from a dog whose death was sampled. This is to be moved somewhere more appropriate; in particular it is only relevant for the discrete case*/

    public static void changeSampledDeath(ForwardRootedNode node, boolean value) {
        node.setAttribute("Sampled Death",value);
    }

    /*Check the flag to determine whether this node represents a transmission from a dog whose death was sampled. This is to be moved somewhere more appropriate; in particular it is only relevant for the discrete case*/

    public static Boolean checkSampledDeath(ForwardRootedNode node) {
        return (Boolean)node.getAttribute("Sampled Death");
    }

    public ForwardRootedNode placeRoot(boolean SDesc, boolean SDeath) {
        ForwardRootedNode node = super.placeRoot(SDesc);
        changeSampledDeath(node,SDeath);
        return node;
    }

    public void updateSampling(ForwardRootedNode node) {
        if(!checkSampled(node)){
            throw new IllegalStateException("Node is not sampled");
        }
        ForwardRootedNode currentNode = node.getParent();
        if(currentNode!=null && currentNode.getHeight()==node.getHeight()){
            changeSampledDeath(currentNode, true);
        }
        while(currentNode!=null && !(Boolean)checkSampledDescendant(currentNode)){
            if(currentNode.getHeight()!=node.getHeight()){
                changeSampledDescendant(currentNode,true);
            }
            currentNode = currentNode.getParent();
        }
    }
}
