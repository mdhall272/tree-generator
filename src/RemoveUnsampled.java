import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 05/12/2011
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 *
 * This removes all unsampled external nodes from the tree, and then removes any nodes with only one parent and one child (as these will not be observed)
 */
public class RemoveUnsampled {

    private ForwardRootedTree newTree;

    public RemoveUnsampled() {
        newTree = new ForwardRootedTree();
    }

    /*Given an existing tree, a node in that tree, and a node in the new tree (which should be standalone, or behaviour
    * will be weird), clone the structure of the first tree from the old node down, excluding any unsampled external
    * nodes and subtrees with no sampled external nodes.*/

    private void removeUnsampledTips(ForwardRootedTree oldTree, ForwardRootedNode oldNode, ForwardRootedNode newNode) {
        /*Check that there are actually any sampled external nodes at all.*/
        boolean samplesExist=false;
        for (Node node: oldTree.getExternalNodes()) {
            if(TreeSimulation.checkSampled((ForwardRootedNode) node)) {
                samplesExist=true;
            }
        }
        if(!samplesExist){
            throw new NoSamplesException();
        }
        /*Copy infector information from the old node onto the new*/
        newNode.setAttribute("Infector",oldNode.getAttribute("Infector"));
        /*Copy the taxon from the old node onto the new*/
        if(oldTree.isExternal(oldNode)){
            if(TreeSimulation.checkSampled(oldNode)) {
                String taxon = oldTree.getTaxon(oldNode).toString();
                newTree.setTaxon(newNode,taxon);
            } else {
                throw new RuntimeException("No samples");
            }
        } else {
            //Get the children of the old node
            List<Node> oldChildren = oldTree.getChildren(oldNode);
            for(Node node : oldChildren){
                //If a child is external, check its sampling status and delete it if unsampled.
                if (oldTree.isExternal(node)) {
                    if((TreeSimulation.checkSampled((ForwardRootedNode) node))) {
                        String taxon = oldTree.getTaxon(node).toString();
                        ForwardRootedNode newChildNode = TreeSimulation.placeChild(newTree, newNode, oldTree.getLength(node), true, true, taxon);
                        try {
                            TreeSimulation.setEdgeNumber(newTree.getEdge(newNode, newChildNode), TreeSimulation.getEdgeNumber(oldTree.getEdge(oldNode, node)));
                        } catch(Graph.NoEdgeException e) {
                            System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                        }
                    }
                }
                /*If a child is internal, check whether it has any sampled descendants (in the discrete case, including
                the sampling of the dog itself which occurs at the same time as the transmission). If it has none,
                delete it. */
                else {
                    if(TreeSimulation.checkSampledDescendant((ForwardRootedNode) node)){
                        ForwardRootedNode newChildNode = TreeSimulation.placeChild(newTree, newNode, oldTree.getLength(node), false, false, null);
                        try {
                            TreeSimulation.setEdgeNumber(newTree.getEdge(newNode, newChildNode), TreeSimulation.getEdgeNumber(oldTree.getEdge(oldNode, node)));
                        } catch(Graph.NoEdgeException e) {
                            System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                        }
                        removeUnsampledTips(oldTree, (ForwardRootedNode)node, newChildNode);
                        newChildNode.setAttribute("Infector",node.getAttribute("Infector"));
                    }
                }
            }
        }
    }

    /*Once unsampled tips and subtrees have gone, one is left with internal nodes that have only one child, and hence
    * should be removed. This method does that.*/

    public static void removeUnnecessaryInternalNodes(ForwardRootedTree tree) {
        for(Node node: tree.getInternalNodes()){
            if(node.getDegree()==2 && tree.getParent(node)!=null){
                Node parent = tree.getParent(node);
                Node child = tree.getChildren(node).get(0);
                /*An edge, before this method is called, represents the existence of a the virus in a single animal.
                * Once internal nodes get deleted, this is no longer true and this information needs to be recorded.
                * This part does that.*/
                int totalEdgeNumber=0;
                try {
                    int parentEdgeNumber = TreeSimulation.getEdgeNumber(tree.getEdge(parent, node));
                    int childEdgeNumber = TreeSimulation.getEdgeNumber(tree.getEdge(node, child));
                    totalEdgeNumber = parentEdgeNumber+childEdgeNumber;
                    if(tree.isExternal(child)) {
                        if(!tree.getTaxon(child).toString().equals(node.getAttribute("Infector").toString())){
                            totalEdgeNumber++;
                        }
                    } else {
                        if(!child.getAttribute("Infector").toString().equals(node.getAttribute("Infector").toString())){
                            totalEdgeNumber++;
                        }
                    }
                }
                catch(Graph.NoEdgeException e) {
                    System.out.println("Edge problem.");
                }
                /*Rewire the surrounding nodes to each other and delete the current one.*/
                ((ForwardRootedNode)child).setLength(((ForwardRootedNode)node).getLength() + ((ForwardRootedNode)child).getLength());
                ((ForwardRootedNode)parent).removeChild((ForwardRootedNode)node);
                ((ForwardRootedNode)parent).addChild((ForwardRootedNode)child);
                ((ForwardRootedNode)child).setParent((ForwardRootedNode)parent);
                try {
                    TreeSimulation.setEdgeNumber(tree.getEdge(parent, child), totalEdgeNumber);
                } catch(Graph.NoEdgeException e) {
                    System.out.println("Edge problem.");
                }
                tree.removeNode((ForwardRootedNode)node);
            }
        }

    }

    /*Method for taking a tree and stripping out all its unsampled information.*/

    public ForwardRootedTree transform(ForwardRootedTree tree) {
        ForwardRootedNode rootNode = newTree.createRoot();
        removeUnsampledTips(tree, ((ForwardRootedNode)tree.getRoot()), rootNode);
        removeUnnecessaryInternalNodes(newTree);
        return newTree;
    }
}