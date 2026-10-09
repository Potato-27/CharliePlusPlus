package nodes;
import lexer.*;

public class AssignmentNode implements Expr {
    public final Expr variable;
    public final Token modifier;
    public final Expr value;

    public AssignmentNode(Expr variable, Token modifier, Expr value){
        this.variable = variable;
        this.modifier = modifier;
        this.value = value;
    }
}
