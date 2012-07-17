import dr.math.distributions.GammaDistribution;
import jebl.math.Random;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 16/07/2012
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class Branch {

    private double incubationPeriod;
    private double infectiousPeriod;
    private double[] branchWaits;

    /*Given that a transmission has just happened, set up a new branch with times of new transmissions and, finally,
the death of the dog. branchWaits is an array containing how long to wait until the next event happens. All events
except the last are transmissions.*/

    public Branch(double incubation_k, double incubation_theta, double infectious_k, double infectious_theta,
                  double R0){
        incubationPeriod=GammaDistribution.nextGamma(incubation_k, incubation_theta);
        infectiousPeriod=GammaDistribution.nextGamma(infectious_k, infectious_theta);
        double randomNumber = Random.nextDouble();
        DrawPoisson fish = new DrawPoisson(R0*infectiousPeriod/(infectious_k*infectious_theta));
        int numberOfChildren = fish.draw(randomNumber);
        double[] branchTimes = new double[numberOfChildren+2];
        branchTimes[0]=0;
        for(int i=1; i<numberOfChildren+1; i++) {
            double randomNumber2 = Random.nextDouble();
            branchTimes[i] = incubationPeriod + (infectiousPeriod*randomNumber2);
        }
        branchTimes[numberOfChildren+1]=incubationPeriod+infectiousPeriod;
        Arrays.sort(branchTimes);
        branchWaits = new double[numberOfChildren+1];
        for(int i=0; i<=numberOfChildren; i++) {
            branchWaits[i]=branchTimes[i+1]-branchTimes[i];
        }
    }

    public double[] getBranchWaits(){
        return branchWaits;
    }

    public double getIncubationPeriod(){
        return incubationPeriod;
    }

    public double getInfectiousPeriod(){
        return infectiousPeriod;
    }

}
