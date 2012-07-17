import dr.evolution.util.Taxon;
import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.trees.Tree;
import jebl.math.Random;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 01/12/2011
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class TreeSimulation {

    protected double samplingStartTime;
    protected double samplingProbability;
    protected double R0;
    protected ForwardRootedTree tree;
    protected int unsampledCorpseCounter;
    protected int sampledCorpseCounter;
    protected int continuingBranches;

    public TreeSimulation(double samplingProbability, double samplingStartTime, double R0){
        if(samplingProbability<0 || samplingProbability>1){
            throw new IllegalArgumentException("Sampling probability must be between 0 and 1");
        }
        if(samplingStartTime<0){
            throw new IllegalArgumentException("Sampling start time cannot be negative");
        }
        this.samplingProbability=samplingProbability;
        this.samplingStartTime=samplingStartTime;
        this.R0=R0;
        tree = new ForwardRootedTree();
        unsampledCorpseCounter = 1;
        sampledCorpseCounter = 1;
        continuingBranches = 1;
    }

    public boolean drawIfSampled(){
        double randomNumber = Random.nextDouble();
        return (randomNumber<=samplingProbability);
    }

    public ForwardRootedNode placeRoot() {
        ForwardRootedNode node=tree.createRoot();
        return node;
    }

    public ForwardRootedNode placeRoot(boolean SDesc) {
        ForwardRootedNode node=tree.createRoot();
        changeSampledDescendant(node,SDesc);
        return node;
    }

    /*Change the sampling status of this node. This is to be moved somewhere more appropriate*/

    public static void changeSampling(ForwardRootedNode node, boolean value) {
        node.setAttribute("Sampled",value);
    }

    /*Change the flag to determine whether this has any sampled descendant nodes. This is to be moved somewhere more appropriate*/

    public static void changeSampledDescendant(ForwardRootedNode node, boolean value) {
        node.setAttribute("Sampled Descendant",value);
    }

    /*Check the sampling status of this node. This is to be moved somewhere more appropriate*/

    public static Boolean checkSampled(ForwardRootedNode node) {
        return (Boolean)node.getAttribute("Sampled");
    }

    /*Check the flag to determine whether this has any sampled descendant nodes. This is to be moved somewhere more appropriate*/

    public static Boolean checkSampledDescendant(ForwardRootedNode node) {
        return (Boolean)node.getAttribute("Sampled Descendant");
    }


    /*Given that a node is sampled, update the "sampled descendant" status of nodes higher up the chain*/

    public void updateSampling(ForwardRootedNode node) {
        if(!checkSampled(node)){
            throw new IllegalStateException("Node is not sampled");
        }
        ForwardRootedNode currentNode = node.getParent();
        while(currentNode!=null && !(Boolean)checkSampledDescendant(currentNode)){
            if(currentNode.getHeight()!=node.getHeight()){
                changeSampledDescendant(currentNode,true);
            }
            currentNode = currentNode.getParent();
        }
    }

    public static ForwardRootedNode placeInternalChild(ForwardRootedTree tree, ForwardRootedNode parent, double length) {
        ForwardRootedNode node = tree.createChild(parent,length);
        changeSampledDescendant(node, false);
        tree.changeExternal(node,false);
        return node;
    }

    public static ForwardRootedNode placeExternalChild(ForwardRootedTree tree, ForwardRootedNode parent, double length, Boolean sampled) {
        ForwardRootedNode node = tree.createChild(parent,length);
        changeSampling(node,sampled);
        tree.changeExternal(node,true);
        return node;
    }

    public static ForwardRootedNode placeChild(ForwardRootedTree tree, ForwardRootedNode parent, double length, boolean external, boolean hasTaxon, String taxonName){
        ForwardRootedNode node = tree.createChild(parent,length);
        tree.changeExternal(node,external);
        if(hasTaxon){
            tree.setTaxon(node,taxonName);
        }
        return node;
    }

    /*Sets and gets edge numbers*/

    public static void setEdgeNumber(Edge edge, int number) {
        edge.setAttribute("Number of transmissions", number);
    }

    public static int getEdgeNumber(Edge edge) {
        Object output = edge.getAttribute("Number of transmissions");
        return ((Number)output).intValue();
    }

}
