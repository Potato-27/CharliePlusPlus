package environment;
import java.util.Map;

import java.util.HashMap;

public class Environment {
    public Map<String, Type> map; // Map of variables/functions to their associated type for this environment
    private Environment enclosing;

    public Environment(){
        this.map = new HashMap<>();
        this.enclosing = null;
    }

    public Environment(Environment parent){
        this.map = new HashMap<>();
        this.enclosing = parent;
    }

    // public method for adding an entry to the type map
    public void define(String name, Type type){
        map.put(name, type);
    }

    // public method for checking whether a variable exists within the environment and return its type
    public Type lookup(String name){
        if (map.containsKey(name)){
            return map.get(name);
        } else if (enclosing != null) {
            return enclosing.lookup(name);
        } else {
            return null;
        }
    }



}
