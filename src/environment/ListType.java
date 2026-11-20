package environment;
public class ListType implements Type {
    public Type elementType;

    public ListType(Type elementType){
        this.elementType = elementType;
    }

    @Override
    public String toString(){
        return "List<" + elementType + ">";
    }
}