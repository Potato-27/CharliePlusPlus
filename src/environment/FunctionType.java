package environment;
import java.util.List;

public class FunctionType implements Type {
    public List<Type> args;
    public Type returnType;

    public FunctionType(List<Type> args, Type returnType){
        this.args = args;
        this.returnType = returnType;
    }

    @Override public String toString(){
        String returnString = "(";
        for (int i = 0; i < args.size(); i++){
            returnString += args.get(i).toString();
            if (i != args.size()-1){
                returnString += ", ";
            }
        }
        returnString += ") -> " + returnType.toString();
        return returnString;
    }
}
