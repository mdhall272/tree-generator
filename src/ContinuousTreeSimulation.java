import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 06/12/2011
 * Time: 12:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class ContinuousTreeSimulation extends TreeSimulation {

    protected double incubation_mean;
    protected double incubation_stdev;
    protected HashMap<String, Double> hostIncubationPeriods;
    protected boolean recordIncubationPeriods;
    protected int nodeCount;

    /*Constructor*/

    public ContinuousTreeSimulation(double samplingProbability, double samplingStartTime, double R0,
                                    double incubation_mean, double incubation_stdev, boolean recordInfectiousPeriods){
        super(samplingProbability, samplingStartTime, R0);
        this.incubation_mean=incubation_mean;
        this.incubation_stdev=incubation_stdev;
        hostIncubationPeriods = new HashMap<String, Double>();
        this.recordIncubationPeriods = recordInfectiousPeriods;
    }

    /*Sum over an array */

    public double arraySum(double[] array){
        double sum = 0;
        for (double i : array) {
            sum = sum+i;
        }
        return sum;
    }
}
