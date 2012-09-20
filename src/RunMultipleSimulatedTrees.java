import jebl.evolution.io.NexusExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 18/01/2012
 * Time: 12:51
 * To change this template use File | Settings | File Templates.
 */
public class RunMultipleSimulatedTrees {

    private double samplingStartTime;
    private double samplingProbability;
    private double R0;
    private double incubation_k;
    private double incubation_theta;
    private double infectious_k;
    private double infectious_theta;
    private int finalNumberOfSamples;
    private int numberOfRuns;
    private PrintWriter writer2;
    private NexusExporter nexp4;

    public RunMultipleSimulatedTrees (double samplingProbability, double samplingStartTime, int finalNumberOfSamples, double R0, double incubation_k, double incubation_theta, double infectious_k, double infectious_theta, int numberOfRuns){
        this.samplingProbability=samplingProbability;
        this.samplingStartTime=samplingStartTime;
        this.finalNumberOfSamples=finalNumberOfSamples;
        this.R0=R0;
        this.incubation_k=incubation_k;
        this.incubation_theta=incubation_theta;
        this.infectious_k=infectious_k;
        this.infectious_theta=infectious_theta;
        this.numberOfRuns=numberOfRuns;
        try {
            writer2 = new PrintWriter(new FileWriter("TestTreeSet.nex"));
            nexp4 = new NexusExporter(writer2);
        } catch (IOException e) {
            System.out.println("File problem");
        }

    }

    private HashSet<ForwardRootedTree> doMultipleRuns(){
        HashSet<ForwardRootedTree> trees = new HashSet<ForwardRootedTree>();
        for (int i=0; i<numberOfRuns; i++){
            System.out.println("R" + (i+1) + " ");
            boolean goodTree=false;
            ForwardRootedTree tree=null;
            int j=0;
            while(!goodTree){
                System.out.print("T" + (j+1) + " ");
                ContinuousTreeSimulationSampleLimited rabiesSim = new ContinuousTreeSimulationSampleLimited(
                        samplingProbability, samplingStartTime, finalNumberOfSamples, R0, incubation_k,
                        incubation_theta, infectious_k, infectious_theta, false);
                tree = rabiesSim.runSimulationNoOutput();
                try {
                    if(tree.getExternalNodes().size()==finalNumberOfSamples){
                        System.out.println("A");
                        goodTree=true;
                    } else {
                        System.out.println("R");
                        j++;
                    }
                } catch(NullPointerException e){
                    System.out.println("R");
                    j++;
                }

            }
/*            try {
                nexp4.exportTree(tree);
                writer2.flush();
            } catch (IOException e) {
                System.out.println("File problem");
            }*/

           trees.add(tree);
        }
        return trees;

    }

    public static void main (String [] args) {
        RunMultipleSimulatedTrees simulationRunner = new RunMultipleSimulatedTrees(0.5, 0, 117, 1.1, 1, 1, 1, 1, 100);
//        simulationRunner.doMultipleRuns();
       WriteToNexusFile.writeMultiple(simulationRunner.doMultipleRuns(), "TestTreeSet.nex");
    }

}
