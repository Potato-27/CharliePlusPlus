package nodes;
import java.util.List;

public class BlockNode implements Stmt{
    public final List<Stmt> block;
    
    public BlockNode(List<Stmt> block){
        this.block = block;
    }

    public String toString(){
        return "[Block: " + block.toString() + "]";
    }
}