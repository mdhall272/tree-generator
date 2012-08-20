import au.com.bytecode.opencsv.CSVWriter;
import jebl.evolution.graphs.Node;
import sun.net.idn.StringPrep;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 16/07/2012
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public class TreeToNetwork {

    public static void outputCSVNetwork(String fileName, ForwardRootedTree tree){
        try{
            CSVWriter csvOut = new CSVWriter(new FileWriter(fileName));
            String[] header = new String[2];
            header[0] = "Child";
            header[1] = "Parent";
            csvOut.writeNext(header);
            for(Node node: tree.getExternalNodes()){
                String childID = tree.getTaxon(node).getName();
                String infectorID = getInfector(node, tree);
                String[] line = new String[2];
                line[0]=childID;
                if(infectorID==null){
                    line[1]="Start";
                } else {
                    line[1]=infectorID;
                }
                csvOut.writeNext(line);
            }
            csvOut.close();
        }
        catch(IOException e){
            System.out.println("IOException");
        }
    }

    public static String getInfector(Node node, ForwardRootedTree tree){
        if(tree.isRoot(node)){
            return "Start";
        }
        String id;
        if(tree.isExternal(node)){
            id = tree.getTaxon(node).getName();
        } else {
            id = (String)node.getAttribute("Infector");
        }
        if(!id.equals(tree.getParent(node).getAttribute("Infector"))){
            return (String)tree.getParent(node).getAttribute("Infector");
        } else {
            return getInfector(tree.getParent(node), tree);
        }
    }
}

