package nodes;
import java.util.List;
import java.util.ArrayList;

public class ListNode implements Expr{
    public final List<Expr> list;

    public ListNode(List<Expr> list){
        this.list = list;
    }

    public ListNode(){
        this.list = new ArrayList<>();
    }
}
