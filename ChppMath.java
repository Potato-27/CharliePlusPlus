public class ChppMath {
    
    
    public static Object add(Object a, Object b){
        if (a instanceof ChppArray && b instanceof ChppArray){
            return ChppArray.addElements((ChppArray)a, (ChppArray)b);
        } else if (a instanceof ChppArray){
            return ChppArray.addScalar((ChppArray)a, ((Number)b).doubleValue());
        } else if (b instanceof ChppArray){
            return ChppArray.addScalar(((Number)a).doubleValue(), (ChppArray)b);
        } else if (a instanceof String){
            return (String)a + b;
        } else if (b instanceof String){
            return a + (String)b;
        } else if (a instanceof Integer && b instanceof Integer){
            return (int)a + (int)b;
        } else {
            return ((Number)a).doubleValue() + ((Number)b).doubleValue();
        }
    }

    public static Object mult(Object a, Object b){
        if (a instanceof ChppArray && b instanceof ChppArray){
            return ChppArray.multElements((ChppArray)a, (ChppArray)b);
        } else if (a instanceof ChppArray){
            return ChppArray.multiplyScalar((ChppArray)a, ((Number)b).doubleValue());
        } else if (b instanceof ChppArray){
            return ChppArray.multiplyScalar(((Number)a).doubleValue(), (ChppArray)b);
        } else if (a instanceof Integer && b instanceof Integer){
            return (int)a * (int)b;
        } else {
            return ((Number)a).doubleValue() * ((Number)b).doubleValue();
        }
    }

    public static Object sub(Object a, Object b){
        if (a instanceof ChppArray && b instanceof ChppArray){
            return ChppArray.subElements((ChppArray)a, (ChppArray)b);
        } else if (a instanceof ChppArray){
            return ChppArray.subScalar((ChppArray)a, ((Number)b).doubleValue());
        } else if (b instanceof ChppArray){
            return ChppArray.subScalar(((Number)a).doubleValue(), (ChppArray)b);
        } else if (a instanceof Integer && b instanceof Integer){
            return (int)a - (int)b;
        } else {
            return ((Number)a).doubleValue() - ((Number)b).doubleValue();
        }
    }

    public static Object div(Object a, Object b){
        if (a instanceof ChppArray && b instanceof ChppArray){
            return ChppArray.divElements((ChppArray)a, (ChppArray)b);
        } else if (a instanceof ChppArray){
            return ChppArray.divScalar((ChppArray)a, ((Number)b).doubleValue());
        } else if (b instanceof ChppArray){
            return ChppArray.divScalar(((Number)a).doubleValue(), (ChppArray)b);
        } else if (a instanceof Integer && b instanceof Integer){
            return (int)a / (int)b;
        } else {
            return ((Number)a).doubleValue() / ((Number)b).doubleValue();
        }
    }

    public static Object negate(Object a){
        if (a instanceof Integer){
            return -(int)a;
        } else if (a instanceof Double){
            return -((Number)a).doubleValue();
        } else if (a instanceof ChppArray){
            return ChppArray.multiplyScalar((ChppArray)a, -1);
        }
        return null;
    }
}
