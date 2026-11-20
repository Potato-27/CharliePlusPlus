package nodes;
import lexer.*;
import environment.*;

public class UnaryNode implements Expr{
    public final Expr right;
    public final Token operator;
    public Type inferredType;
    
    public UnaryNode(Expr right, Token operator){
        this.right = right;
        this.operator = operator;
    }
}