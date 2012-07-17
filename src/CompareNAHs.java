import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: mhall
 * Date: 19/01/2012
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public class CompareNAHs implements Comparator<NodeAndHeight> {
    public int compare(NodeAndHeight NNAH1, NodeAndHeight NNAH2){
        if(NNAH1.getHeight()<NNAH2.getHeight()){
            return -1;
        }
        if(NNAH1.getHeight()==NNAH2.getHeight()){
            return 0;
        }
        if(NNAH1.getHeight()>NNAH2.getHeight()){
            return 1;
        }
        return 0;
    }
}