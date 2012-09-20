import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 05/01/2012
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class SeqTrackTreeVisualizer {

    public static void main (String[] args){
        try {

            CSVReader reader = new CSVReader(new FileReader("/Users/mhall/Documents/Netherlands H7N7/PB2 segment/PB2 SeqTrack.csv"));
            HashMap<Integer, String> names = new HashMap<Integer, String>();
            HashMap<Integer, Integer> ancestors = new HashMap<Integer, Integer>();
            HashMap<Integer, GregorianCalendar> dates = new HashMap<Integer, GregorianCalendar>();
            HashMap<Integer, ForwardRootedNode> nodes = new HashMap<Integer, ForwardRootedNode>();
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
            Date firstDate = null;
            GregorianCalendar firstDateCal = new GregorianCalendar();
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                names.put(Integer.parseInt(nextLine[1]), nextLine[0]);
                if(nextLine[2].equals("NA")){
                    ancestors.put(Integer.parseInt(nextLine[1]), null);
                } else {
                ancestors.put(Integer.parseInt(nextLine[1]), Integer.parseInt(nextLine[2]));
                }
                Date sampleDate = dateParser.parse(nextLine[4]);
                if(firstDate==null || sampleDate.compareTo(firstDate)<0){
                    firstDate=sampleDate;
                    firstDateCal.setTime(firstDate);
                }
                GregorianCalendar sampleDateCalendar = new GregorianCalendar();
                sampleDateCalendar.setTime(sampleDate);
                dates.put(Integer.parseInt(nextLine[1]), sampleDateCalendar);
            }
            ForwardRootedTree tree = new ForwardRootedTree();
            for (int i=1; i<=names.size(); i++){
                ForwardRootedNode node = new ForwardRootedNode();
                nodes.put(i,node);
                tree.addNode(node);
                tree.setTaxon(node, names.get(i));
                double height=(double)TimeUnit.MILLISECONDS.toDays((dates.get(i).getTimeInMillis() - firstDateCal.getTimeInMillis()));
                node.setHeight(height);
            }
            for (int i=1; i<=names.size(); i++){
                if(ancestors.get(i)!=null){
                    nodes.get(i).setParent(nodes.get(ancestors.get(i)));
                    nodes.get(ancestors.get(i)).addChild(nodes.get(i));
                    nodes.get(i).setLength(nodes.get(i).getHeight()-nodes.get(ancestors.get(i)).getHeight());
                }
            }
            for (int i=1; i<=names.size(); i++) {
                if(!tree.isExternal(nodes.get(i))){
                    ForwardRootedNode node = tree.createChild(nodes.get(i),0);
                    tree.setTaxon(node, names.get(i));
                }
            }
            WriteToNexusFile.write(tree, "/Users/mhall/Documents/Netherlands H7N7/PB2 segment/PB2 SeqTrack.tre");

        }
        catch (FileNotFoundException e) {
             System.out.println("File not found");
        }
        catch (IOException e) {
                System.out.println("IOE");
            }
        catch (ParseException e) {
            System.out.println("Parse exception");
        }

    }
}

