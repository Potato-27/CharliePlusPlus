package environment;

public class TypeVariable implements Type {
    final public int id;

    public TypeVariable(int id){
        this.id = id;
    }


    @Override
    public String toString(){
        return "T" + id;
    }
}
