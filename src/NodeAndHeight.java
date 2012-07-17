import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 19/01/2012
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */

//This isn't a map because you need to be able to sort on the height

public class NodeAndHeight {
    public double height;
    public ForwardRootedNode node;
    
    public NodeAndHeight(ForwardRootedNode node, double height){
        this.node=node;
        this.height=height;
    }

    public double getHeight(){
        return height;
    }

    public ForwardRootedNode getNode(){
        return node;
    }

}
