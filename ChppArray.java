import java.util.List;
import java.util.ArrayList;

public class ChppArray {
    public List<Object> list;

    public ChppArray(Object... objects){
        list = new ArrayList<>();
        for (Object object : objects){
            list.add(object);
        }
    }

    public ChppArray(){
        list = new ArrayList<>();
    }

    // obtaining the value at a given index
    public Object get(int index){
        if (index < 0) return list.get(index + list.size());
        return list.get(index);
    }

    // changing the value of a given index
    public void set(int index, Object object){
        if (index < 0) {
            list.set(index + list.size(), object); 
            return;
        }
        list.set(index, object);
    }

    // adding elements to the list
    public void add(Object object){
        list.add(object);
    }

    // returns size of ChppArray
    public int size(){
        return list.size();
    }

    // multiplying all values by a given scalar
    public static ChppArray multiplyScalar(ChppArray array, double value){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(multiplyScalar(arr, value));
            } else {
                newArr.add(((Number) obj).doubleValue() * value);
            }
        }
        return newArr;
    }

    public static ChppArray multiplyScalar(double value, ChppArray array){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(multiplyScalar(value, arr));
            } else {
                newArr.add(value * ((Number) obj).doubleValue());
            }
        }
        return newArr;
    }

    // adding all values by a given scalar
    public static ChppArray addScalar(ChppArray array, double value){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(addScalar(arr, value));
            } else {
                newArr.add(((Number) obj).doubleValue() + value);
            }
        }
        return newArr;
    }

    public static ChppArray addScalar(double value, ChppArray array){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(addScalar(value, arr));
            } else {
                newArr.add(value + ((Number) obj).doubleValue());
            }
        }
        return newArr;
    }

    // subtracting all values by or from a given scalar
    public static ChppArray subScalar(ChppArray array, double value){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(subScalar(arr, value));
            } else {
                newArr.add(((Number) obj).doubleValue() - value);
            }
        }
        return newArr;
    }

    public static ChppArray subScalar(double value, ChppArray array){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(subScalar(value, arr));
            } else {
                newArr.add(value - ((Number) obj).doubleValue());
            }
        }
        return newArr;
    }

    // dividing all values by or from a given scalar
    public static ChppArray divScalar(ChppArray array, double value){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(divScalar(arr, value));
            } else {
                newArr.add(((Number) obj).doubleValue() / value);
            }
        }
        return newArr;
    }

    public static ChppArray divScalar(double value, ChppArray array){
        ChppArray newArr = new ChppArray();
        for (Object obj : array.list){
            if (obj instanceof ChppArray arr){
                newArr.add(divScalar(value, arr));
            } else {
                newArr.add(value / ((Number) obj).doubleValue());
            }
        }
        return newArr;
    }

    // adds all elements of two equal sized arrays together
    public static ChppArray addElements(ChppArray arr1, ChppArray arr2){
        ChppArray newArr = new ChppArray();
        if (arr1.size() != arr2.size()){
            return null;
        }
        for (int i = 0; i < arr1.size(); i++){
            if (arr1.get(i) instanceof ChppArray){
                ChppArray arr = addElements((ChppArray)arr1.get(i), (ChppArray)arr2.get(i));
                if (arr == null){
                    return null;
                }
                newArr.add(arr);
            } else if (arr1.get(i) instanceof String){
                newArr.add((String)arr1.get(i) + (String)arr2.get(i));
            } else {
                newArr.add(((Number) arr1.get(i)).doubleValue() + ((Number) arr2.get(i)).doubleValue());
            }
        }
        return newArr;
    }

    public static ChppArray subElements(ChppArray arr1, ChppArray arr2){
        ChppArray newArr = new ChppArray();
        if (arr1.size() != arr2.size()){
            return null;
        }
        for (int i = 0; i < arr1.size(); i++){
            if (arr1.get(i) instanceof ChppArray){
                ChppArray arr = subElements((ChppArray)arr1.get(i), (ChppArray)arr2.get(i));
                if (arr == null){
                    return null;
                }
                newArr.add(arr);
            } else {
                newArr.add(((Number) arr1.get(i)).doubleValue() - ((Number) arr2.get(i)).doubleValue());
            }
        }
        return newArr;
    }

    public static ChppArray multElements(ChppArray arr1, ChppArray arr2){
        ChppArray newArr = new ChppArray();
        if (arr1.size() != arr2.size()){
            return null;
        }
        for (int i = 0; i < arr1.size(); i++){
            if (arr1.get(i) instanceof ChppArray){
                ChppArray arr = multElements((ChppArray)arr1.get(i), (ChppArray)arr2.get(i));
                if (arr == null){
                    return null;
                }
                newArr.add(arr);
            } else {
                newArr.add(((Number) arr1.get(i)).doubleValue() * ((Number) arr2.get(i)).doubleValue());
            }
        }
        return newArr;
    }

    public static ChppArray divElements(ChppArray arr1, ChppArray arr2){
        ChppArray newArr = new ChppArray();
        if (arr1.size() != arr2.size()){
            return null;
        }
        for (int i = 0; i < arr1.size(); i++){
            if (arr1.get(i) instanceof ChppArray){
                ChppArray arr = divElements((ChppArray)arr1.get(i), (ChppArray)arr2.get(i));
                if (arr == null){
                    return null;
                }
                newArr.add(arr);
            } else {
                newArr.add(((Number) arr1.get(i)).doubleValue() / ((Number) arr2.get(i)).doubleValue());
            }
        }
        return newArr;
    }



    @Override
    public String toString(){
        String returnStr = "[";
        for (int i = 0; i < list.size(); i++){
            if (i == list.size()-1){
                returnStr += list.get(i);
            } else {
                returnStr += list.get(i) + ", ";
            }
        }
        returnStr += "]";
        return returnStr;
    }

    public static ChppArray negate(ChppArray a){
        return multiplyScalar(a, -1);
    }


}
