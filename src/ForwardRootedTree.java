import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.util.AttributableHelper;
import sun.font.CreatedFontTracker;

import java.util.*;

/**
 *
 * Implementation of RootedTree allowing trees to be created forwards (whether nodes are external is not known
 * at the outset).  Intended for use in simulations.  Nodes internal to branches (one ancestor, one descendant)
 * are allowed
 *
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 30/11/2011
 * Time: 10:57
 * To change this template use File | Settings | File Templates.
 */
public class ForwardRootedTree implements RootedTree {

    protected final Set<Node> nodes = new LinkedHashSet<Node>();
    private AttributableHelper helper = null;
    private boolean hasHeights = true;
    private boolean hasLengths = true;
    private boolean heightsKnown = true;
    private boolean lengthsKnown = true;
    private boolean rootExists = false;
    private boolean conceptuallyUnrooted = false;

/*
Create an empty tree. (Contructors to make copies to follow if they become necessary)
*/


    public ForwardRootedTree() {
    }

    public ForwardRootedTree(RootedTree tree) {
        createNodes(tree, tree.getRootNode());
    }

    /*Create the root. Height and length are 0.*/

    public ForwardRootedNode createRoot() {
        if(rootExists) {
            throw new IllegalStateException("Root already exists");
        }
        ForwardRootedNode node = new ForwardRootedNode();
        nodes.add(node);
        node.setHeight(0);
        node.setLength(0);
        rootExists = true;
        return node;
    }

    public void addNode(ForwardRootedNode node){
        nodes.add(node);
    }

    /*Create the root in versions without sampling information. Height and length are 0.*/

/*    public void createRootNoSampling() {
        if(rootExists) {
            throw new IllegalStateException("Root already exists");
        }
        ForwardRootedNode node = new ForwardRootedNode();
        nodes.add(node);
        node.setHeight(0);
        node.setLength(0);
        rootExists = true;
    }*/

    /*Create the root with specified height and length (which, for the root, are the same).*/

    public ForwardRootedNode createRoot(double length) {
        if(rootExists) {
            throw new IllegalStateException("Root already exists");
        }
        ForwardRootedNode node = new ForwardRootedNode();
        nodes.add(node);
        node.setHeight(length);
        node.setLength(length);
        rootExists = true;
        return node;
    }

    /*Create a child node.*/

    public ForwardRootedNode createChild(ForwardRootedNode parent, double length) {
        ForwardRootedNode node = new ForwardRootedNode(parent);
        nodes.add(node);
        parent.addChild(node);
        node.setLength(length);
        node.setHeight(parent.getHeight()+length);
        return node;
    }

/*    *//*Create any other node without sampling information - an internal node.*//*

    public ForwardRootedNode createChild(ForwardRootedNode parent, double length) {
        ForwardRootedNode node = new ForwardRootedNode(parent);
        nodes.add(node);
        parent.addChild(node);
        node.setLength(length);
        node.setHeight(parent.getHeight()+length);
        node.changeSampledDescendant(false);
        node.changeSampledDeath(false);
        node.changeExternal(false);
        return(node);
    }

    *//*Create any other node with sampling information - an external node.*//*

    public ForwardRootedNode createChild(Boolean sampled, ForwardRootedNode parent, double length) {
        ForwardRootedNode node = new ForwardRootedNode(parent);
        nodes.add(node);
        parent.addChild(node);
        node.setLength(length);
        node.setHeight(parent.getHeight()+length);
        node.changeExternal(true);
        node.changeSampling(sampled);
        return(node);
    }

    *//*Create any other node ignoring sampling entirely, external/internal can be set, taxon added immediately*//*

    public ForwardRootedNode createChild(ForwardRootedNode parent, double length, boolean external) {
        ForwardRootedNode node = new ForwardRootedNode(parent);
        nodes.add(node);
        parent.addChild(node);
        node.setLength(length);
        node.setHeight(parent.getHeight()+length);
        node.changeExternal(external);
        return(node);
    }

    *//*Create any other node ignoring sampling entirely, external/internal can be set, taxon added immediately*//*

    public ForwardRootedNode createChild(ForwardRootedNode parent, double length, boolean external, String taxon) {
        ForwardRootedNode node = new ForwardRootedNode(parent);
        nodes.add(node);
        parent.addChild(node);
        node.setLength(length);
        node.setHeight(parent.getHeight()+length);
        node.changeExternal(external);
        node.addTaxon(taxon);
        return(node);
    }*/

    /*Methods required for interfaces (mostly copy and pasted from SimpleRootedTree)*/

    public Set<Node> getNodes() {
        return nodes;
    }

    public Node[] getNodes(Edge edge) {
        for (Node node : getNodes()) {
            if (((ForwardRootedNode)node).getEdge() == edge) {
                return new Node[] { node, ((ForwardRootedNode)node).getParent() };
            }
        }
        return null;
    }

    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = new LinkedHashSet<Node>();
        for (Node node : getNodes()) {
            final int deg = node.getDegree() ;
            if (deg == degree) nodes.add(node);
        }
        return nodes;
    }


    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new AttributableHelper();
        }
        helper.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    public void removeAttribute(String name) {
        if( helper != null ) {
            helper.removeAttribute(name);
        }
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        if (helper == null) {
            return Collections.emptyMap();
        }
        return helper.getAttributeMap();
    }

    public double getHeight(Node node) {
        return ((ForwardRootedNode)node).getHeight();
    }

    public Node getParent(Node node) {
        return ((ForwardRootedNode)node).getParent();
    }

    public Taxon getTaxon(Node node) {
        return ((ForwardRootedNode)node).getTaxon();
    }

    public void setTaxon(Node node, String taxonName){
        ((ForwardRootedNode)node).addTaxon(taxonName);
    }

    public Set<Taxon> getTaxa() {
        HashSet<Taxon> outputSet = new HashSet<Taxon>();
        for (Node node : nodes) {
            if(((ForwardRootedNode)node).checkExternal()){
                outputSet.add(((ForwardRootedNode)node).getTaxon());
            }

        }
        return outputSet;
    }

    public Node getNode(Taxon taxon) {
        return null;
    }

    public void renameTaxa(Taxon from, Taxon to){
    }

    public boolean isExternal(Node node) {
        try{
        return ((ForwardRootedNode)node).checkExternal();
        } catch(NullPointerException e){
            System.out.println("??");
            return false;
        }
    }

    public void changeExternal(Node node, boolean value) {
        ((ForwardRootedNode)node).changeExternal(value);
    }

    public boolean hasHeights() {
        return hasHeights;
    }

    public boolean hasLengths() {
        return hasLengths;
    }

    public Set<Node> getInternalNodes() {
        Set<Node> internalNodes = new LinkedHashSet<Node>();
        for (Node node : getNodes()) {
            if (!((ForwardRootedNode)node).checkExternal()){
                internalNodes.add(node);
            }
        }
        return internalNodes;
    }

    public Set<Node> getExternalNodes() {
        Set<Node> externalNodes = new LinkedHashSet<Node>();
        for (Node node : getNodes()) {
            if (((ForwardRootedNode)node).checkExternal()){
                externalNodes.add(node);
            }
        }
        return externalNodes;
    }

    public Set<Edge> getInternalEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getInternalNodes()) {
            if (node != getRootNode()) {
                edges.add(((ForwardRootedNode)node).getEdge());
            }
        }
        return edges;
    }

    public List<Node> getChildren(Node node) {
        return new ArrayList<Node>(((ForwardRootedNode)node).getChildren());
    }

    public List<Node> getAdjacencies(Node node) {
        return ((ForwardRootedNode)node).getAdjacencies();
    }

    public Node getRootNode(){
        for (Node node: getNodes())
            if(((ForwardRootedNode)node).getParent()==null){
                return node;
            }
        return null;
    }

    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        if (((ForwardRootedNode)node1).getParent() == node2) {
            if (heightsKnown) {
                return ((ForwardRootedNode)node2).getHeight() - ((ForwardRootedNode)node1).getHeight();
            } else {
                return ((ForwardRootedNode)node1).getLength();
            }
        } else if (((ForwardRootedNode)node2).getParent() == node1) {
            if (heightsKnown) {
                return ((ForwardRootedNode)node1).getHeight() - ((ForwardRootedNode)node2).getHeight();
            } else {
                return ((ForwardRootedNode)node2).getLength();
            }
        } else {
            throw new NoEdgeException();
        }
    }

    public boolean conceptuallyUnrooted() {
        return conceptuallyUnrooted;
    }

    public Set<Edge> getEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getNodes()) {
            if (node != getRootNode()) {
                edges.add(((ForwardRootedNode)node).getEdge());
            }
        }
        return edges;
    }

    public double getLength(Node node) {
        if (!hasLengths) throw new IllegalArgumentException("This tree has no branch lengths");
        return ((ForwardRootedNode)node).getLength();
    }

    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        if (((ForwardRootedNode)node1).getParent() == node2) {
            return ((ForwardRootedNode)node1).getEdge();
        } else if (((ForwardRootedNode)node2).getParent() == node1) {
            return ((ForwardRootedNode)node2).getEdge();
        } else {
            throw new NoEdgeException();
        }
    }

    public List<Edge> getEdges(Node node) {
        List<Edge> edges = new ArrayList<Edge>();
        for (Node adjNode : getAdjacencies(node)) {
            edges.add(((ForwardRootedNode)adjNode).getEdge());

        }
        return edges;
    }

    public Set<Edge> getExternalEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getExternalNodes()) {
            edges.add(((ForwardRootedNode)node).getEdge());
        }
        return edges;
    }

    public boolean isRoot(Node node){
        if (((ForwardRootedNode)node).getParent()==null) {
            return true;
        }else
            return false;

    }

    public ForwardRootedNode getParent(ForwardRootedNode node){
        ForwardRootedNode parent = node.getParent();
        return parent;
    }


    public Node getRoot(){
        for(Node node:nodes){
            if(isRoot(node)){
                return node;
            }
        } return null;
    }

    public void removeChild(Node parent, Node child) {
        ((ForwardRootedNode)parent).removeChild((ForwardRootedNode)child);
    }

    public void removeNode(ForwardRootedNode node) {
        nodes.remove(node);
    }

    /**
     * Clones the entire tree structure from the given RootedTree.
     * @param tree
     * @param node
     * @return
     */
    private Node createNodes(RootedTree tree, Node node) {

        ForwardRootedNode newNode;
        if (tree.isExternal(node)) {
            newNode = new ForwardRootedNode();
            newNode.changeExternal(true);
            newNode.addTaxon(tree.getTaxon(node).toString());

        } else {
            newNode = new ForwardRootedNode();
            newNode.changeExternal(false);
            for (Node child : tree.getChildren(node)) {
                ForwardRootedNode newChild = (ForwardRootedNode)createNodes(tree, child);
                newChild.setParent(newNode);
                newNode.addChild(newChild);
                try {
                    for(Map.Entry<String,Object>e:getEdge(child,node).getAttributeMap().entrySet()) {
                        getEdge(newChild,newNode).setAttribute(e.getKey(), e.getValue());
                    }
                }catch(NoEdgeException f) {
                    System.out.println("Edge problem");
                }

            }
        }
        newNode.setHeight(tree.getHeight(node));
        newNode.setLength(tree.getLength(node));
        boolean b=newNode.checkExternal();
        nodes.add(newNode);
        return newNode;
    }

}