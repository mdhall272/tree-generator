import jebl.evolution.graphs.Graph;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 16/01/2012
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public class ContinuousTreeSimulationTimeLimited extends ContinuousTreeSimulation {

    protected double samplingEndTime;

    public ContinuousTreeSimulationTimeLimited(double samplingProbability, double samplingStartTime,
                                                    double samplingEndTime, double R0, double incubation_k,
                                                    double incubation_theta, double infectious_k,
                                                    double infectious_theta, boolean recordInfectiousPeriod){
        super(samplingProbability, samplingStartTime, R0, incubation_k, infectious_k, incubation_theta,
                infectious_theta, recordInfectiousPeriod);
        if(samplingEndTime<=samplingStartTime){
            throw new IllegalArgumentException("Sampling cannot end before it begins");
        }
        this.samplingEndTime=samplingEndTime;
    }

    public ContinuousTreeSimulationTimeLimited(double samplingProbability, double samplingStartTime,
                                               double samplingEndTime, double R0, double incubation_k,
                                               double incubation_theta, double infectious_k,
                                               double infectious_theta){
        this(samplingProbability, samplingStartTime, samplingEndTime, R0, incubation_k, infectious_k, incubation_theta,
                infectious_theta, false);
        }

    public void simulateInfection(ForwardRootedNode transmissionNode) {
        Branch branch = new Branch(incubation_k, incubation_theta, infectious_k, infectious_theta, R0);
        double[] branchWaits = branch.getBranchWaits();
        ForwardRootedNode currentNode = transmissionNode;
        boolean sampled = false;
        double timeTillDeath = arraySum(branchWaits);

        //Determine if the dog was sampled after death
        if(tree.getHeight(transmissionNode)+timeTillDeath>=samplingStartTime && tree.getHeight(transmissionNode)+timeTillDeath<=samplingEndTime) {
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
        boolean outOfTime = false;
        int i=0;
        while(!outOfTime && i<branchWaits.length){
            //What to do if the current child node occurs after sampling (and thus the simulation) has finished.
            if(tree.getHeight(currentNode)+branchWaits[i]>samplingEndTime){
                ForwardRootedNode finalNode = placeExternalChild(tree,currentNode,samplingEndTime-tree.getHeight(currentNode),false);
                try {
                    setEdgeNumber(tree.getEdge(finalNode,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                tree.setTaxon(finalNode,"CL"+continuingBranches);
                continuingBranches++;
                outOfTime=true;
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
                simulateInfection(currentChild);
                currentNode=currentChild;
                i++;
            }
            //What to do if the current child node represents the death of the dog
            else {
                ForwardRootedNode deadDog = placeExternalChild(tree,currentNode,branchWaits[branchWaits.length-1], sampled);
                try {
                    setEdgeNumber(tree.getEdge(deadDog,currentNode),0);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                }
                if(sampled){
                    updateSampling(deadDog);
                }
                tree.setTaxon(deadDog,ID);
                i++;
            }
        }
    }

    public int[] runSimulation (boolean runDiscrete, boolean runSamplesOnly, boolean runDiscreteSamplesOnly,
                                String basicfilename, String prunedfilename, String discretefilename,
                                String discreteprunedfilename){

        int[] nodeNumbers = new int[2];

        /*Place the root and get it. This root represents the transmission that started the tree and does not result in
a bifurcation. Done like this since its length is actually informative.*/
        ForwardRootedNode rootNode = placeRoot(false);
        //Simulate all the infection
        simulateInfection(rootNode);
        nodeNumbers[0]=tree.getExternalNodes().size();

        //Write to file

        WriteToNexusFile.write(tree, basicfilename);

        if (runDiscrete && !runSamplesOnly){
            ForwardRootedTree discreteTree = CreateDiscreteCopy.makeDiscreteCopy(tree);
            WriteToNexusFile.write(discreteTree, discretefilename);
        }

        if (runSamplesOnly && !runDiscrete){
            RemoveUnsampled RUNs = new RemoveUnsampled();
            try {
                ForwardRootedTree prunedTree = RUNs.transform(tree);
                nodeNumbers[1]=prunedTree.getExternalNodes().size();
                WriteToNexusFile.write(prunedTree, prunedfilename);
            }
            catch (NoSamplesException e) {
                nodeNumbers[1]=0;
            }
        }

        if (runSamplesOnly && runDiscreteSamplesOnly) {
            ForwardRootedTree discreteTree = CreateDiscreteCopy.makeDiscreteCopy(tree);
            WriteToNexusFile.write(discreteTree, discretefilename);

            RemoveUnsampled RUNs = new RemoveUnsampled();
            RemoveUnsampled RUNs2 = new RemoveUnsampled();
            try {
                ForwardRootedTree prunedTree = RUNs.transform(tree);
                nodeNumbers[1]=prunedTree.getExternalNodes().size();
                WriteToNexusFile.write(prunedTree, prunedfilename);
                ForwardRootedTree prunedDiscreteTree = RUNs2.transform(discreteTree);
                WriteToNexusFile.write(prunedDiscreteTree, discreteprunedfilename);
            }
            catch (NoSamplesException e) {
                nodeNumbers[1]=0;
            }
        }
        return nodeNumbers;
    }

}
