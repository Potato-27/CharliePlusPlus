package nodes;
import environment.*;

public class IndexNode implements Expr{
    public final Expr target;
    public final Expr index;
    public Type inferredType = PrimitiveType.VOID;

    public IndexNode(Expr target, Expr index){
        this.target = target;
        this.index = index;
    }
}
