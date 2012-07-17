import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 17/01/2012
 * Time: 14:46
 * To change this template use File | Settings | File Templates.
 */
public class TrimToTimeLimit {
    
    private double timeLimit;
    private ForwardRootedTree newTree;
    private int continuingLineages;
    
    public TrimToTimeLimit(double timeLimit) {
        this.timeLimit=timeLimit;
        newTree = new ForwardRootedTree();
        continuingLineages=1;
    }

    private boolean removeLaterTips (ForwardRootedTree oldTree, ForwardRootedNode oldNode, ForwardRootedNode newNode) {
        /*Check that there will be any nodes left when the later nodes are removed.*/
        boolean tipsRemain=false;
        boolean sampledTipsFurtherDown=false;
        newNode.setAttribute("Node number", oldNode.getAttribute("Node number"));
        for (Node node: oldTree.getExternalNodes()) {
            if(oldTree.getHeight(oldNode)<=timeLimit) {
                tipsRemain=true;
            }
        }
        if(!tipsRemain){
            throw new NoSamplesException();
        }
        /*Copy infector information from the old node onto the new*/
        newNode.setAttribute("Infector",oldNode.getAttribute("Infector"));
        /*Copy the taxon from the old node onto the new*/
        if(oldTree.isExternal(oldNode)){
            if(oldTree.getHeight(oldNode)<=timeLimit) {
                String taxon = oldTree.getTaxon(oldNode).toString();
                newTree.setTaxon(newNode,taxon);
            } else {
                throw new RuntimeException("No samples");
            }
        } else {
            //Get the children of the old node
            List<Node> oldChildren = oldTree.getChildren(oldNode);
            for(Node node : oldChildren){
                //If a child is external, check its date and, if it's too late, replace it with a CL taxon.
                if (oldTree.isExternal(node)) {
                    if(oldTree.getHeight(node)<=timeLimit) {
                        String taxon = oldTree.getTaxon(node).toString();
                        ForwardRootedNode newChildNode = TreeSimulation.placeChild(newTree, newNode, oldTree.getLength(node), true, true, taxon);
                        try {
                            TreeSimulation.setEdgeNumber(newTree.getEdge(newNode, newChildNode), TreeSimulation.getEdgeNumber(oldTree.getEdge(oldNode, node)));
                        } catch(Graph.NoEdgeException e) {
                            System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                        }
                        newChildNode.setAttribute("Sampled", node.getAttribute("Sampled"));
                        if((Boolean)newChildNode.getAttribute("Sampled")){
                            sampledTipsFurtherDown=true;
                        }
                    } else {
                        ForwardRootedNode newCLNode = TreeSimulation.placeChild(newTree, newNode, timeLimit - oldTree.getHeight(oldNode), true, true, "CL" + continuingLineages);
                        newCLNode.setAttribute("Sampled", false);
                        continuingLineages++;
                    }
                } else {
                    if(oldTree.getHeight(node)<=timeLimit) {
                        ForwardRootedNode newChildNode = TreeSimulation.placeChild(newTree, newNode, oldTree.getLength(node), false, false, null);
                        try {
                            TreeSimulation.setEdgeNumber(newTree.getEdge(newNode, newChildNode), TreeSimulation.getEdgeNumber(oldTree.getEdge(oldNode, node)));
                        } catch(Graph.NoEdgeException e) {
                            System.out.println("Something went wrong with the edges. You really shouldn't be seeing this.");
                        }
                        boolean newSampledDescendant = removeLaterTips(oldTree, (ForwardRootedNode)node, newChildNode);
                        if(newSampledDescendant){
                            sampledTipsFurtherDown=true;
                        }
                        newChildNode.setAttribute("Infector",node.getAttribute("Infector"));
                    } else {
                        ForwardRootedNode newCLNode = TreeSimulation.placeChild(newTree, newNode, timeLimit - oldTree.getHeight(oldNode), true, true, "CL" + continuingLineages);
                        newCLNode.setAttribute("Sampled", false);
                        continuingLineages++;
                    }
                }
            }
        }
        newNode.setAttribute("Sampled Descendant", sampledTipsFurtherDown);
        return sampledTipsFurtherDown;
    }

    public ForwardRootedTree trim(ForwardRootedTree tree) {
        ForwardRootedNode rootNode = newTree.createRoot();
        removeLaterTips(tree, ((ForwardRootedNode)tree.getRoot()), rootNode);
        RemoveUnsampled.removeUnnecessaryInternalNodes(newTree);
        return newTree;
    }
}
