package nodes;
import lexer.*;

public class UnaryNode implements Expr{
    public final Expr right;
    public final Token operator;
    
    public UnaryNode(Expr right, Token operator){
        this.right = right;
        this.operator = operator;
    }
}