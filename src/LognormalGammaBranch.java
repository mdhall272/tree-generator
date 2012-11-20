import dr.math.MathUtils;
import dr.math.distributions.GammaDistribution;
import dr.math.distributions.LogNormalDistribution;
import dr.math.distributions.NormalDistribution;
import jebl.math.Random;
import org.joda.time.tz.NameProvider;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 15/11/2012
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class LognormalGammaBranch extends Branch {

    public LognormalGammaBranch(double incubation_mean, double incubation_stdev, double infectious_k, double infectious_theta,
                            double R0){
        incubationPeriod= Math.exp(incubation_stdev*MathUtils.nextGaussian()+incubation_mean);
        infectiousPeriod= GammaDistribution.nextGamma(infectious_k, infectious_theta);
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
}
