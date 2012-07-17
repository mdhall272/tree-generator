import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 06/12/2011
 * Time: 12:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class ContinuousTreeSimulation extends TreeSimulation {

    protected double incubation_k;
    protected double incubation_theta;
    protected double infectious_k;
    protected double infectious_theta;
    protected HashMap<String, Double> hostInfectiousPeriods;
    protected boolean recordInfectiousPeriods;
    protected int nodeCount;

    /*Constructor*/

    public ContinuousTreeSimulation(double samplingProbability, double samplingStartTime, double R0,
                                    double incubation_k, double incubation_theta, double infectious_k,
                                    double infectious_theta, boolean recordInfectiousPeriods){
        super(samplingProbability, samplingStartTime, R0);
        this.incubation_k=incubation_k;
        this.infectious_k=infectious_k;
        this.incubation_theta=incubation_theta;
        this.infectious_theta=infectious_theta;
        hostInfectiousPeriods = new HashMap<String, Double>();
        this.recordInfectiousPeriods = recordInfectiousPeriods;
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
