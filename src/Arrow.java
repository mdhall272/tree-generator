/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 15/08/2013
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public class Arrow {

    private float proportionToDisplay;
    private double[] start;
    private double[] end;
    private String XtoY;
    private Visuals visualiser;
    private int startVisible;
    private int endVisible;

    public Arrow(double[] start, double[] end, float proportionToDisplay, Visuals visualiser, int startVisible,
                 int endVisible){
        this.start = start;
        this.end = end;
        this.startVisible = startVisible;
        this.endVisible = endVisible;
        this.proportionToDisplay = proportionToDisplay;
        this.visualiser = visualiser;
    }

    public Arrow(SpatialCase origin, SpatialCase destination, float proportionToDisplay, Visuals visualiser,
                 int startVisible, int endVisible){
        this(origin.getCoords(), destination.getCoords(), proportionToDisplay, visualiser, startVisible, endVisible);
        XtoY = origin.getNumber()+"_to_"+destination.getNumber();
    }

    public void display(int time){
        if(time >= startVisible && time<=endVisible){
            visualiser.stroke(0);
            visualiser.drawArrow(start, end, proportionToDisplay);
        }
    }

}
