package nodes;
import lexer.*;
import java.util.List;
import java.util.ArrayList;

public class ClassDeclNode implements Stmt{
    public final Token name;
    public final List<VarDeclNode> fields;
    public final List<FunDeclNode> methods;

    public ClassDeclNode(Token name){
        this.name = name;
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public ClassDeclNode(Token name, List<VarDeclNode> variables, List<FunDeclNode> functions){
        this.name = name;
        this.fields = variables;
        this.methods = functions;
    }

    @Override
    public String toString() {
    return "[ClassDecl: " + name.lexeme + " Variables: " + fields + " Functions: " + methods + "]";
    }
}
