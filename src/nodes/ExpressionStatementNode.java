package nodes;

public class ExpressionStatementNode implements Stmt {
    public final Expr expression;

    public ExpressionStatementNode(Expr expression){
        this.expression = expression;
    }
}
