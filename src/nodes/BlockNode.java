package nodes;
import java.util.List;
import environment.*;

public class BlockNode implements Stmt{
    public final List<Stmt> block;
    public Environment localEnv = null;
    
    public BlockNode(List<Stmt> block){
        this.block = block;
    }

    @Override
    public String toString(){
        return "[Block: " + block.toString() + "]";
    }
}