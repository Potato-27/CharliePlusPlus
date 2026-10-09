package nodes;

public class IfNode implements Stmt {
    public final Expr condition;
    public final Stmt trueStatement;
    public final Stmt falseStatement;
    
    public IfNode(Expr condition, Stmt trueStatement, Stmt falseStatement){
        this.condition = condition;
        this.trueStatement = trueStatement;
        this.falseStatement = falseStatement;
    }
    
    public IfNode(Expr condition, Stmt trueStatement){
        this.condition = condition;
        this.trueStatement = trueStatement;
        this.falseStatement = null;
    }
}