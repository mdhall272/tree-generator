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
public class TreeToDataTableMorelli {

    public static void outputCSVDataTable(String fileName, ForwardRootedTree tree, double reportToCull,
                                          DateTime simulationStart, HashMap<String,Double> incubationPeriodLookup,
                                          HashMap<String, Integer> dLookup, boolean datesToTips){
        try{
            CSVWriter csvOut = new CSVWriter(new FileWriter(fileName));
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
            String[] header = new String[11];
            header[0] = "Farm_ID";
            header[1] = "Taxon";
            header[2] = "Exam_date";
            header[3] = "Cull_date";
            header[4] = "Exact_infection_height";
            header[5] = "Exact_exam_height";
            header[6] = "Exact_cull_height";
            header[7] = "Actual_oldest_lesion";
            header[8] = "Incubation_period";
            header[9] = "Clinical_guess";
            csvOut.writeNext(header);

            for(Node node:tree.getExternalNodes()){
                double nodeHeight = tree.getHeight(node);
                double heightWhenInfected = -1;
                String[] line = new String[11];
                String caseName = tree.getTaxon(node).getName();
                line[0] = caseName;
                line[1] = caseName+"_"+Math.floor(nodeHeight);
                DateTime reportDate = new DateTime(simulationStart.plusDays((int)Math.floor(nodeHeight)));
                line[2] = formatter.print(reportDate);
                if(datesToTips){
                    tree.setTaxon(node, caseName+"_"+Math.floor(nodeHeight));
                }
                DateTime cullDate = new DateTime(simulationStart.plusDays((int)Math.floor(nodeHeight+
                        reportToCull)));
                line[3] = formatter.print(cullDate);
                boolean infectorFound=false;
                Node parent = tree.getParent(node);
                while(!infectorFound){
                    if(!parent.getAttribute("Infector").equals(tree.getTaxon(node).getName()) || tree.isRoot(parent)){
                        heightWhenInfected = tree.getHeight(parent);
                        line[4] = Double.toString(heightWhenInfected);
                        infectorFound = true;
                    } else {
                        parent = tree.getParent(parent);
                    }
                }

                line[5] = Double.toString(nodeHeight);
                line[6] = Double.toString(nodeHeight+reportToCull);
                double correctValue = nodeHeight - heightWhenInfected - incubationPeriodLookup.get(line[0]);
                line[7] = Double.toString(correctValue);
                line[8] = Double.toString(incubationPeriodLookup.get(line[0]));
                line[9] = Integer.toString(dLookup.get(caseName));
                csvOut.writeNext(line);
            }
            csvOut.close();
        } catch(IOException e){
            System.out.println("IOException");
        }

    }
}
