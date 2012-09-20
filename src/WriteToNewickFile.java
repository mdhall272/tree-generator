import jebl.evolution.io.NewickExporter;
import jebl.evolution.io.NexusExporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 06/01/2012
 * Time: 11:21
 * To change this template use File | Settings | File Templates.
 */
public class WriteToNewickFile {

    public static void write(ForwardRootedTree tree, String fileName){
        try {PrintWriter writer2 = new PrintWriter(new FileWriter(new File(fileName)));
            NewickExporter nexp4 = new NewickExporter(writer2);
            nexp4.exportTree(tree);
            writer2.flush();
        }
        catch (IOException e) {
            System.out.println("File problem");
        }
    }

    public static void writeMultiple(HashSet<ForwardRootedTree> trees, String fileName){
        try {PrintWriter writer2 = new PrintWriter(new FileWriter(fileName));
            NexusExporter nexp4 = new NexusExporter(writer2);
            nexp4.exportTrees(trees);
            writer2.flush();
        }
        catch (IOException e) {
            System.out.println("File problem");
        }
    }

}
