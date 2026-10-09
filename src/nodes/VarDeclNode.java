package nodes;
import lexer.*;

public class VarDeclNode implements Stmt{
    public final Token identifier;
    public final Expr initializer;
    
    public VarDeclNode(Token identifier, Expr initializer){
        this.identifier = identifier;
        this.initializer = initializer;
    }
    
    public VarDeclNode(Token identifier){
        this.identifier = identifier;
        this.initializer = null;
    }

    public String toString(){
        if (initializer != null){
            return "[VarDecl: " + identifier.lexeme + " = " + initializer.toString() + "]";
        }
        return "[VarDecl: " + identifier.lexeme + "]";
    }
}