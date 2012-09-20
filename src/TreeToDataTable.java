import au.com.bytecode.opencsv.CSVWriter;
import dr.math.MathUtils;
import jebl.evolution.graphs.Node;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
                                          double estimateJitter, DateTime simulationStart,
                                          HashMap<String,Double> infectiousPeriodLookup){
        try{
            CSVWriter csvOut = new CSVWriter(new FileWriter(fileName));
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            String[] header = new String[5];
            header[0] = "Farm_ID";
            header[1] = "Taxon";
            header[2] = "Exam_date";
            header[3] = "Cull_date";
            header[4] = "Oldest_lesion";
            csvOut.writeNext(header);

            for(Node node:tree.getExternalNodes()){
                String[] line = new String[5];
                line[0] = tree.getTaxon(node).getName();
                line[1] = tree.getTaxon(node).getName();
                double test = tree.getHeight(node);
                DateTime reportDate = new DateTime(simulationStart.plusDays((int)Math.floor(tree.getHeight(node))));
                line[2] = formatter.print(reportDate);
                DateTime cullDate = new DateTime(simulationStart.plusDays((int)Math.floor(tree.getHeight(node)+
                        reportToCull)));
                line[3] = formatter.print(cullDate);
                double correctValue = infectiousPeriodLookup.get(line[0]);
                double estimatedLesionDate = -1;
                while(estimatedLesionDate<0){
                    double jitter = estimateJitter*MathUtils.nextGaussian();
                    estimatedLesionDate = correctValue+jitter;
                }
                line[4] = Double.toString(Math.round(estimatedLesionDate));
                csvOut.writeNext(line);
            }
            csvOut.close();
        } catch(IOException e){
            System.out.println("IOException");
        }

    }
}
