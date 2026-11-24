package nodes;

public class RangeNode implements Expr {
    public final Expr left;
    public final Expr right;

    public RangeNode(Expr left, Expr right){
        this.left = left;
        this.right = right;
    }
}
