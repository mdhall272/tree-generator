
import dr.math.distributions.Distribution;
import dr.math.distributions.ExponentialDistribution;
import processing.core.PApplet;
import processing.core.PFont;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 15/08/2013
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public class Visuals extends PApplet {

    private SpatialSim simulation;
    private PFont f;
    private HashSet<Arrow> arrows;

    public Visuals(){
        HashMap<Integer, Distribution> latentPeriods = new HashMap<Integer, Distribution>();
        latentPeriods.put(0, null);

        HashMap<Integer, Distribution> infectiousPeriods = new HashMap<Integer, Distribution>();
        infectiousPeriods.put(0, new ExponentialDistribution(0.2));

        simulation = new SpatialSim(20, 0.05, 0.1, latentPeriods, infectiousPeriods, 15, 0.01, this, "01/12/2011", null, null, null, null, null);
        simulation.runSim();

        arrows = new HashSet<Arrow>();

        for(SpatialCase destination : simulation.getCases()){
            SpatialCase origin = simulation.getWIW().get(destination);
            if(origin!=null){
                arrows.add(new Arrow(origin, destination, (float)0.8, this));
            }
        }
    }


    public void setup(){
        size(1000,1000);
        f = createFont("Arial",10,true);
    }

    public void draw(){
        background(255);
        for(SpatialCase thisCase : simulation.getCases()){
            thisCase.display(f);
        }
        for(Arrow arrow : arrows){
            arrow.display();
        }
        save("image.tif");
    }


    static public void main(String[] args) {
        PApplet.main(new String[] { "--present", "Visuals" });
    }

    void drawArrow(double[] start, double[] end, float proportionToDisplay){
        stroke(0, 0, 0);
        float[] floatStart = translateToImageSpace(start);
        float[] floatEnd = translateToImageSpace(end);

        float fullLength = (float)Math.sqrt(Math.pow(floatStart[0]-floatEnd[0],2)+Math.pow(floatStart[1]-floatEnd[1],2));
        float displayLength = fullLength*proportionToDisplay;

        float angle = getAngle(floatStart, floatEnd);


        float displayStartX = (floatStart[0]) + ((1-proportionToDisplay)/2)*(floatEnd[0]-floatStart[0]);
        float displayStartY = (floatStart[1]) + ((1-proportionToDisplay)/2)*(floatEnd[1]-floatStart[1]);

        pushMatrix();
        translate(displayStartX, displayStartY);
        rotate(angle);
        line(0,0,displayLength, 0);
        line(displayLength, 0, displayLength - 8, -8);
        line(displayLength, 0, displayLength - 8, 8);
        popMatrix();
    }

    public static float[] translateToImageSpace(double[] array){
        float[] out = new float[array.length];
        for(int i=0; i<array.length; i++){
            out[i] = (float)array[i]*900 +50;
        }
        return out;
    }

    public static float getAngle(float[] vector){
        double value = Math.atan(vector[1]/vector[0]);
        if(vector[0]>0){
            return (float)value;
        } else {
            return (float)(Math.PI + value);
        }
    }

    public static float getAngle(float[] start, float[] end){
        float[] translation = {end[0]-start[0], end[1]-start[1]};
        return getAngle(translation);
    }



}


