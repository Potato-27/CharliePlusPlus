package environment;

public class TypeVariable implements Type {
    final public int id;
    public Constraint constraint;
    
    public TypeVariable(int id){
        this.id = id;
        this.constraint = Constraint.NONE;
    }
    
    public TypeVariable(int id, Constraint constraint){
        this.id = id;
        this.constraint = constraint;
    }


    @Override
    public String toString(){
        return "T" + id;
    }
}
