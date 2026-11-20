package nodes;
import lexer.*;
import environment.*;

public class BinaryNode implements Expr{
    public final Expr left;
    public final Expr right;
    public final Token operator;
    public Type leftType;
    public Type rightType;
    public Type inferredType;
    
    public BinaryNode(Expr left, Expr right, Token operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
    }
}