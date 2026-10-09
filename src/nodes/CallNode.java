package nodes;
import java.util.List;
import java.util.ArrayList;

public class CallNode implements Expr {
    public final Expr name;
    public final List<Expr> arguments;
    
    public CallNode(Expr name, List<Expr> arguments){
        this.name = name;
        this.arguments = arguments;
    }
    
    public CallNode(Expr name){
        this.name = name;
        this.arguments = new ArrayList<>();
    }
}