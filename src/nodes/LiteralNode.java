package nodes;

public class LiteralNode implements Expr {
    public final Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    public String toString(){
        return "[Literal: " + value + "]";
    }
}