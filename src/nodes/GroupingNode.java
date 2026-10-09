package nodes;

public class GroupingNode implements Expr {
    public final Expr expression;
    
    public GroupingNode(Expr expression){
        this.expression = expression;
    }
}