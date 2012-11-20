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
public abstract class Branch {

    protected double incubationPeriod;
    protected double infectiousPeriod;
    protected double[] branchWaits;

    /*Given that a transmission has just happened, set up a new branch with times of new transmissions and, finally,
the death of the dog. branchWaits is an array containing how long to wait until the next event happens. All events
except the last are transmissions.*/


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
