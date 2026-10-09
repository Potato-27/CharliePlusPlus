package nodes;
import lexer.*;
import java.util.List;
import java.util.ArrayList;

public class FunDeclNode implements Stmt {
    public final Token name;
    public final List<Token> parameters;
    public final BlockNode block;
    
    public FunDeclNode(Token name, BlockNode block, List<Token> parameters){
        this.name = name;
        this.block = block;
        this.parameters = parameters;
    }
    
    public FunDeclNode(Token name, BlockNode block){
        this.name = name;
        this.block = block;
        this.parameters = new ArrayList<>();
    }

    public String toString(){
        return "[FunDecl: " + name.lexeme + " " + block.toString() + "]";
    }
}