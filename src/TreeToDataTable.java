import au.com.bytecode.opencsv.CSVWriter;
import dr.math.MathUtils;
import dr.math.distributions.NormalDistribution;
import jebl.evolution.graphs.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: mhall
 * Date: 16/07/2012
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class TreeToDataTable {

    public static void outputCSVDataTable(String fileName, ForwardRootedTree tree, double reportToCull,
                                          double estimateJitter,
                                          HashMap<String,Double> infectiousPeriodLookup){
        try{
            CSVWriter csvOut = new CSVWriter(new FileWriter(fileName));
            String[] header = new String[4];
            header[0] = "Host";
            header[1] = "Time of examination";
            header[2] = "Time of cull";
            header[3] = "Estimated oldest lesion age";
            csvOut.writeNext(header);

            for(Node node:tree.getExternalNodes()){
                String[] line = new String[4];
                line[0] = tree.getTaxon(node).getName();
                line[1] = Double.toString(tree.getHeight(node));
                line[2] = Double.toString(tree.getHeight(node)+reportToCull);
                double correctValue = infectiousPeriodLookup.get(line[0]);
                double jitter = estimateJitter*MathUtils.nextGaussian();
                line[3] = Double.toString(correctValue+jitter);
                csvOut.writeNext(line);
            }
            csvOut.close();
        } catch(IOException e){
            System.out.println("IOException");
        }

    }
}
