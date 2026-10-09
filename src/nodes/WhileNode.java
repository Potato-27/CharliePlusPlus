package nodes;

public class WhileNode implements Stmt {
    public final Expr condition;
    public final Stmt trueStatement;
    
    public WhileNode(Expr condition, Stmt trueStatement){
        this.condition = condition;
        this.trueStatement = trueStatement;
    }
}