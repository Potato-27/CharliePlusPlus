package environment;
import java.util.List;
import java.util.ArrayList;

public class Scheme implements Type{
    public Type type;
    public List<TypeVariable> bounds;

    public Scheme(Type type){
        this.type = type;
        bounds = new ArrayList<>();
    }
}
