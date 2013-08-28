import processing.core.PConstants;
import processing.core.PFont;


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

    public void display(PFont f){
        visualiser.ellipseMode(PConstants.CENTER);
        visualiser.rectMode(PConstants.CENTER);
        visualiser.textAlign(PConstants.CENTER, PConstants.CENTER);
        visualiser.fill(255);
        visualiser.stroke(0);
        visualiser.ellipse(((float) coords[0] * 900)+50, ((float) coords[1] * 900)+50, 16, 16);
        visualiser.fill(0);
        visualiser.textFont(f,12);
        visualiser.text(Integer.toString(number), ((float) coords[0] * 900)+50, ((float) coords[1] * 900)+50, (float)15, (float)15);

    }

}
