import jebl.evolution.graphs.Graph;

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

    public void simulateInfection(ForwardRootedNode transmissionNode, double currentR0) {
        Branch branch = new Branch(incubation_k, incubation_theta, infectious_k, infectious_theta, currentR0);
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
                simulateInfection(currentChild, currentChild.getHeight()<cutoff ? R0 : 0);
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

    public boolean runSimulation (boolean runSamplesOnly, String basicFileName, String prunedFileName,
                               String networkOutputFileName, String dataTableOutputFile,
                               double reportToCull, double estimateJitter,int minTipsForOutput,
                               int maxTipsForOutput){

        int[] nodeNumbers = new int[2];

        /*Place the root and get it. This root represents the transmission that started the tree and does not result in
        a bifurcation. Done like this since its length is actually informative.*/
        ForwardRootedNode rootNode = placeRoot(false);
        //Simulate all the infection
        rootNode.setAttribute("Node number", nodeCount);
        nodeCount++;
        simulateInfection(rootNode, R0);
        nodeNumbers[0]=tree.getExternalNodes().size();
        System.out.println("Nodes in original tree: " + nodeNumbers[0]);
        if(tree.getExternalNodes().size()>=minTipsForOutput && tree.getExternalNodes().size()<=maxTipsForOutput){
            System.out.println("Writing to file: "+basicFileName);

            WriteToFile.write(tree, basicFileName);

            ForwardRootedTree prunedTree = tree;

            /*For purposes of generating trees for network reconstruction with 100% sampling, you need to do this to
          remove continuing lineages*/

            if (runSamplesOnly){
                RemoveUnsampled RUNs = new RemoveUnsampled();
                try {
                    prunedTree = RUNs.transform(prunedTree);
                    nodeNumbers[1]=prunedTree.getExternalNodes().size();
                    System.out.println("Nodes in samples-only tree: " + nodeNumbers[1]);
                    WriteToFile.write(prunedTree, prunedFileName);
                }
                catch (NoSamplesException e) {
                    nodeNumbers[1]=0;
                    System.out.println("Nodes in samples-only tree: " + nodeNumbers[1]);
                }
            }

            TreeToNetwork.outputCSVNetwork(networkOutputFileName, prunedTree);
            if(recordInfectiousPeriods){
                TreeToDataTable.outputCSVDataTable(dataTableOutputFile, prunedTree, reportToCull, estimateJitter,
                        hostInfectiousPeriods);
            }
            return true;
        } else {
            System.out.println("Skipping");
            return false;
        }

    }

    public static void runSimulations(double samplingProbability, double samplingStartTime,
                                      double cutoff, double R0, double incubation_k,
                                      double incubation_theta, double infectious_k,
                                      double infectious_theta, boolean recordIncubationPeriods,
                                      boolean runSamplesOnly, String basicFileNameRoot, String prunedFileNameRoot,
                                      String networkOutputFileNameRoot, String dataTableOutputFileRoot,
                                      double reportToCull, double estimateJitter, int minTips, int maxTips,
                                      int desiredRuns){
        int run = 1;
        while(run<=desiredRuns){
            ContinuousTreeSimulationR0Cutoff thisSim = new ContinuousTreeSimulationR0Cutoff(samplingProbability,
                    samplingStartTime, cutoff, R0, incubation_k, incubation_theta, infectious_k, infectious_theta,
                    recordIncubationPeriods);
            boolean keep = thisSim.runSimulation(runSamplesOnly, basicFileNameRoot+"_"+run+".nex",
                    prunedFileNameRoot+"_"+run+".nex",networkOutputFileNameRoot+"_"+run+".csv",
                    dataTableOutputFileRoot+"_"+run+".csv", reportToCull, estimateJitter, minTips, maxTips);
            if(keep){
                run++;
            }
        }

    }

    public static void main (String [] args){
        runSimulations(1, 0, 30, 1.5, 1.67, 3, 2, 2, true, false, "allfarms",
                "contlineages", "network", "datatable", 2, 0, 40, 60, 10);
    }
}
