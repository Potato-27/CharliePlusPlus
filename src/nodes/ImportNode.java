package nodes;
import lexer.*;

public class ImportNode implements Stmt {
    public Token name;

    public ImportNode(Token name){
        this.name = name;
    }
}