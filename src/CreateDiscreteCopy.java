import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 08/12/2011
 * Time: 13:39
 * To change this template use File | Settings | File Templates.
 */
public class CreateDiscreteCopy {

    public CreateDiscreteCopy(ForwardRootedTree tree){

    }

    public static ForwardRootedTree makeDiscreteCopy(ForwardRootedTree tree){
        ForwardRootedTree newTree = new ForwardRootedTree(tree);
        Node rootNode = newTree.getRoot();
        Node secondNode = newTree.getChildren(rootNode).get(0);
        changeBranchLengths((ForwardRootedNode)secondNode);
        return newTree;
    }

    public static void changeBranchLengths(ForwardRootedNode node){
        if(node.checkExternal()){
            if(node.getTaxon().toString().equals(node.getParent().getAttribute("Infector"))){
                node.setLength(0);
                node.setHeight(node.getParent().getHeight());
            } else {
                node.setLength(1);
                node.setHeight(node.getParent().getHeight()+1);
            }

        } else {
            if(node.getAttribute("Infector").equals(node.getParent().getAttribute("Infector"))){
                node.setLength(0);
                node.setHeight(node.getParent().getHeight());
            } else {
                node.setLength(1);
                node.setHeight(node.getParent().getHeight()+1);
            }
            for(ForwardRootedNode childNode:node.getChildren()){
                changeBranchLengths(childNode);
            }
        }
    }

}
