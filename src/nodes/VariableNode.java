package nodes;
import lexer.*;

public class VariableNode implements Expr{
    public final Token identifier;
    
    public VariableNode(Token identifier){
        this.identifier = identifier;
    }
}