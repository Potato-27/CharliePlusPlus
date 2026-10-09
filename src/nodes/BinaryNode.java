package nodes;
import lexer.*;

public class BinaryNode implements Expr{
    public final Expr left;
    public final Expr right;
    public final Token operator;
    
    public BinaryNode(Expr left, Expr right, Token operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
    }
}