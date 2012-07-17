/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 01/12/2011
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class DrawPoisson {

    double lambda;

    public DrawPoisson(double lambda){
        this.lambda=lambda;
    }

    public int draw(double number){
        if(number<0 || number>1){
            throw new IllegalArgumentException("Input must be between 0 and 1");
        }
        boolean found=false;
        int counter=0;
        int factorialValue=1;
        double CumPMFValue=0;
        int output = 0;
        while(!found){
            double PMFvalue = Math.pow(lambda,counter)*Math.exp(-lambda)/factorialValue;
            CumPMFValue=CumPMFValue+PMFvalue;
            if(number<=CumPMFValue){
                output = counter;
                found=true;
            }
            counter++;
            factorialValue=factorialValue*counter;
        }
        return output;
    }

}
