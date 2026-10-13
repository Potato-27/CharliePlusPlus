package nodes;

public class ReturnNode implements Stmt {
    public final Expr expression;

    public ReturnNode(Expr expr){
        this.expression = expr;
    }
}
