package nodes;
import lexer.*;
import environment.*;

public class VariableNode implements Expr{
    public final Token identifier;
    public Type inferredType;
    
    public VariableNode(Token identifier){
        this.identifier = identifier;
    }
}