package nodes;

public class NullLiteralNode implements Expr {
    public final Object value;

    public NullLiteralNode() {
        this.value = null;
    }
}