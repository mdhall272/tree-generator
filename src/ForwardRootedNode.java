import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.BaseEdge;
import jebl.evolution.trees.BaseNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 30/11/2011
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public class ForwardRootedNode extends BaseNode {

    private ArrayList<ForwardRootedNode> children;
    private ForwardRootedNode parent;
    private Double height; // start at 0 at the root node in this case
    private Double length; // length of branch to the parent
    private boolean isExternal; // node is external
    private Edge edge = null;
    private Taxon taxon;

    /*Constuctor for a loose node*/

    public ForwardRootedNode() {
        isExternal = true;
        children = new ArrayList<ForwardRootedNode>();
        taxon = null;
    }

    /*Constructor for a child of an existing node*/

    public ForwardRootedNode(ForwardRootedNode parent) {
        this.parent = parent;
        isExternal = true;
        children = new ArrayList<ForwardRootedNode>();
        taxon = null;
    }

    /*Attach the given node as a child of this one. This means that the current node is no longer external, if it was.*/

    public void addChild(ForwardRootedNode node) {
        children.add(node);
        isExternal=false;
    }

    /*Remove a given node from the list of children. Could use an exception if there is no such node.*/

    public void removeChild(ForwardRootedNode node) {
        children.remove(node);
    }

    /*Set the parent of this node to be the given node*/

    public void setParent(ForwardRootedNode node) {
        parent=node;
    }

    /*Return a list of the children of this node*/

    public ArrayList<ForwardRootedNode> getChildren() {
        return children;
    }

    /*Return the parent of this node.*/

    public ForwardRootedNode getParent() {
        return parent;
    }

    /*Return the height of this node (in this mode, the smallest height is the root.*/

    public double getHeight() {
        return height;
    }

    /*Set the height of this node*/

    public void setHeight(double height) {
        this.height = height;
    }

    /*Get the length of this node (the distance from it to its parent node*/

    public double getLength() {
        return length;
    }

    /*Set the length of this node*/

    public void setLength(double length) {
        this.length = length;
    }

    /*Get the degree of the node*/

    public int getDegree() {
        return children.size() +(parent==null?0:1);
    }

    /*Check whether the node is external*/

    public boolean checkExternal() {
        return isExternal;
    }

    /*Change whether the node is external*/

    public void changeExternal(boolean value) {
        isExternal = value;
    }

    /*Add a taxon to the node*/

    public void addTaxon(String name) {        
        taxon = Taxon.getTaxon(name);
    }

    /*Get the taxon from the node*/
    
    public Taxon getTaxon() {
        return taxon;
    }

    /*Get the edge from the node*/

    public Edge getEdge() {

        if (edge == null) {
            edge = new BaseEdge() {
                public double getLength() {
                    return length;
                }

                @Override
                public void setAttribute(String name, Object value) {
                    ForwardRootedNode.this.setAttribute(name, value);
                }

                @Override
                public Object getAttribute(String name) {
                    return ForwardRootedNode.this.getAttribute(name);
                }

                @Override
                public void removeAttribute(String name) {
                    ForwardRootedNode.this.removeAttribute(name);
                }

                @Override
                public Set<String> getAttributeNames() {
                    return ForwardRootedNode.this.getAttributeNames();
                }

                @Override
                public Map<String, Object> getAttributeMap() {
                    return ForwardRootedNode.this.getAttributeMap();
                }
            };
        }

        return edge;

    }

    /*Get the list of adjacent nodes*/

    public List<Node> getAdjacencies() {
        List<Node> adjacencies = new ArrayList<Node>();
        if (children != null) adjacencies.addAll(children);
        if (parent != null) adjacencies.add(parent);
        return adjacencies;
    }
}
