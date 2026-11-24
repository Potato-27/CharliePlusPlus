package nodes;
import lexer.*;

public class ForNode implements Stmt{
    public final Token variable;
    public final Expr iterable;
    public final Expr condition;
    public final BlockNode block;

    public ForNode(Token variable, Expr iterable, BlockNode block, Expr condition){
        this.variable = variable;
        this.iterable = iterable;
        this.block = block;
        this.condition = condition;
    }

    public ForNode(Token variable, Expr iterable, BlockNode block){
        this.variable = variable;
        this.iterable = iterable;
        this.block = block;
        this.condition = null;
    }

}
