import processing.core.PConstants;
import processing.core.PFont;

import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 04/06/2013
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class SpatialCase implements Comparable<SpatialCase> {

    private double[] coords;

    // becomes infectious at the end of infectionDay; stops being infectious at the end of cullDay

    private int infectionDay;
    private int infectiousDay;
    private int cullDay;
    private int number;
    private String name;
    private int latentType;
    private int infectiousType;
    private Visuals visualiser;
    private boolean wasEverInfected;
    private HashMap<Integer, Integer[]> colourMap;


    public SpatialCase(int number, int latentType, int infectiousType, double[] coords, Visuals visualiser){
        this.number = number;
        name = "Location_"+number;
        this.latentType = latentType;
        this.infectiousType = infectiousType;
        this.coords = coords;
        infectionDay = Integer.MAX_VALUE;
        infectiousDay = Integer.MAX_VALUE;
        cullDay = Integer.MAX_VALUE;
        this.visualiser = visualiser;
        wasEverInfected = false;
    }

    public void makeColourMap(int end){
        colourMap = new HashMap<Integer,Integer[]>();
        for(int i=0; i<end; i++){
            if(isSusceptibleAt(i)){
                Integer[] green = {50,205,50};
                colourMap.put(i,green);
            } else if(isLatentAt(i)){
                Integer[] orange = {255,140,0};
                colourMap.put(i,orange);
            } else if(isInfectiousAt(i)){
                Integer[] red = {255,0,0};
                colourMap.put(i,red);
            } else if(isCulledAt(i)){
                Integer[] grey = {125,125,125};
                colourMap.put(i, grey);
            }
        }

    }

    public double[] getCoords(){
        return coords;
    }

    public int getNumber(){
        return number;
    }

    public boolean isInfectiousAt(int time){
        return time>infectiousDay && time<= cullDay;
    }

    public boolean isLatentAt(int time){
        return time>infectionDay && time<=infectiousDay;
    }

    public boolean isInfectedAt(int time){
        return time>infectionDay && time<= cullDay;
    }

    public boolean isSusceptibleAt(int time){
        return time<=infectionDay;
    }

    public boolean isCulledAt(int time){
        return time> cullDay;
    }

    public void setInfectionDay(int time){
        infectionDay = time;
        wasEverInfected = true;
    }

    public boolean wasEverInfected(){
        return wasEverInfected;
    }

    public void setInfectiousDay(int time){
        infectiousDay = time;
    }

    public void setCullDay(int time){
        cullDay = time;
    }

    public int getExamDay(){
        return cullDay;
    }

    public int getLatentType(){
        return latentType;
    }

    public int getInfectiousType(){
        return infectiousType;
    }

    public int getInfectionDay(){
        return infectionDay;
    }

    public int getInfectiousDay(){
        return infectiousDay;
    }

    public int getCullDay(){
        return cullDay;
    }

    public String getName(){
        return name;
    }

    @Override
    public int compareTo(SpatialCase spatialCase) {
        return infectionDay==spatialCase.getInfectionDay() ? 0 : (
                infectionDay<spatialCase.getInfectionDay() ? -1 : 1);
    }

    public void display(PFont f, int time){
        visualiser.ellipseMode(PConstants.CENTER);
        visualiser.rectMode(PConstants.CENTER);
        visualiser.textAlign(PConstants.CENTER, PConstants.CENTER);
        visualiser.fill(colourMap.get(time)[0],colourMap.get(time)[1],colourMap.get(time)[2]);
        visualiser.stroke(0);
        visualiser.strokeWeight(2);
        visualiser.ellipse(((float) coords[0] * 900)+50, ((float) coords[1] * 900)+50, 25, 25);
        visualiser.fill(0);
        visualiser.textFont(f,14);
        visualiser.text(Integer.toString(number), ((float) coords[0] * 900)+50, ((float) coords[1] * 900)+49, (float)25, (float)25);

    }

}
