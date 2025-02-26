package dataquest;

import java.util.ArrayList;

public class Field {
    private String name;
    private String type = null;
    private ArrayList<String> stringArray = new ArrayList<>();
    private ArrayList<Object> typedArray = new ArrayList<>();
    Field(String fieldName) {
        this.name = fieldName;
    }

    ArrayList<Object> getTypedArray() {
        return typedArray;
    }

    ArrayList<String> getStringArray() {
        return stringArray;
    }

    String getType() {
        return type;
    }

    String getName(){
        return name;
    }

    String getCellString(int valIndex){
        if(valIndex >= stringArray.size()){return "ERROR: Index out of bounds";}
        return stringArray.get(valIndex);
    }

    //Passed the new type and returns if it succeeds in identifying the type 
    //If a cell cannot be set to the new type, the index in typedArray is set to null
    //Case sensitive
    boolean setType(String newType){
        boolean validType = (newType.equals("String") ||newType.equals("float") ||newType.equals("boolean"));

        if(!validType){
            System.out.println("ERROR: Type " + newType + " not recognized");
            //If the type hasn't been set yet, set to String to prevent issues in Dataset
            if(type == null){
                type = "String";
            }
            return false;
        }
        if(stringArray.isEmpty()) {
            type = newType;
            return true;
        }
        type = newType;
        //if the newType is a String, there is no need to verify with Patterns
        if(newType.equals("String")) {
            for (int i = 0; i < stringArray.size(); i++) {
                typedArray.set(i, stringArray.get(i));
            }
            return true;
        }

        for (int i = 0; i < stringArray.size(); i++) {
            String realType = Dataset.getPattern(stringArray.get(i));
            if(!realType.equals(newType)){
                System.out.println("ERROR: " + stringArray.get(i) + " can't be parsed to a " + newType + ". Value set to null");
                typedArray.set(i,null);
            } else if(realType.equals("float")) {
                typedArray.set(i,Float.valueOf(stringArray.get(i)));
            } else if(realType.equals("boolean")) {
                typedArray.set(i,Boolean.valueOf(stringArray.get(i)));
            } else {
                System.out.println("ERROR: new valid pattern " + realType + " not added to Field.setType(String newType)");
                return false;
            }
        }
        return true;
    }

    //Adds a new cell to stringArray and typedArray
    //If the new cell does not match the field type, the index in typedArray is set to null
    //Either way, the cell is added as given to stringArray
    boolean addCell(String newValue) {
        stringArray.add(newValue);
        if(type.equals("String")) {
            typedArray.add(newValue);
        } else {
            String realType = Dataset.getPattern(newValue);
            if(!type.equals(realType)) {
                System.out.println("ERROR: " + newValue + " is type " + realType + " and cannot be parsed to type " + type);
                typedArray.add(null);
                return false;
            }
            if(type.equals("float")) {
                typedArray.add(Float.valueOf(newValue));
            } else if(type.equals("boolean")) {
                typedArray.add(Boolean.valueOf(newValue));
            } else {
                System.out.println("ERROR: new data type " + realType + " not handled in Field.addCell(String newValue)");
                return false;
            }
        }
        return true;
    }

    //updates a cell at a specified index to a new value
    //Does not allow updating unless it matches the specified type
    boolean updateCell(int oldValueIndex, String newValue){
        if(oldValueIndex > typedArray.size()){return false;}
        
        if(type.equals("String")) {
            typedArray.set(oldValueIndex,newValue);
            stringArray.set(oldValueIndex,newValue);
        }
        else {
            String realType = Dataset.getPattern(newValue);
            if(!type.equals(realType)) {
                System.out.println("ERROR: " + newValue + " is type " + realType + " and cannot be parsed to type " + type);
                return false;
            }
            if(realType.equals("float")) {
                typedArray.set(oldValueIndex,Float.valueOf(newValue));
                stringArray.set(oldValueIndex,newValue);
            } else if(type.equals("boolean")) {
                typedArray.set(oldValueIndex,Boolean.valueOf(newValue));
                stringArray.set(oldValueIndex,newValue);
            } else {
                System.out.println("ERROR: new data type " + realType + " not handled in Field.updateCell(int oldValueIndex, String newValue)");
                return false;
            }
        }
        return true;
    }

    //Removes a cell by index; returns true if the cell successfully deletes and false if the index is out of range
    boolean deleteCell(int oldValueIndex){
        if(oldValueIndex >= stringArray.size()){return false;}
        stringArray.remove(oldValueIndex);
        typedArray.remove(oldValueIndex);
        return true;
    }
}
