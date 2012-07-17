import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 08/12/2011
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class DiscreteRemoveUnsampled extends RemoveUnsampled {

    private ForwardRootedTree newTree;

    public DiscreteRemoveUnsampled(){
        this.newTree = new ForwardRootedTree();
    }

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
        /*Copy the taxon from the old node onto the new (if present; this only happens with the simplest possible tree.*/
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
                    if((Boolean)(TreeSimulation.checkSampled((ForwardRootedNode) node))) {
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
                    boolean a = TreeSimulation.checkSampledDescendant((ForwardRootedNode) node);
                    boolean b = DiscreteTreeSimulation.checkSampledDeath((ForwardRootedNode) node);
                    {
                        if((TreeSimulation.checkSampledDescendant((ForwardRootedNode) node)) || DiscreteTreeSimulation.checkSampledDeath((ForwardRootedNode) node)) {
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
    }

    public ForwardRootedTree transform(ForwardRootedTree tree, boolean cont) {
        ForwardRootedNode rootNode = newTree.createRoot();
        removeUnsampledTips(tree, ((ForwardRootedNode)tree.getRoot()), rootNode);
        removeUnnecessaryInternalNodes(newTree);
        return newTree;
    }
}
