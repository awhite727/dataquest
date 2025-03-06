package dataquest;

import java.io.Serializable;
import java.util.ArrayList;

public class Field implements Serializable {
    private String name;
    private String type = null;
    private ArrayList<String> stringArray = new ArrayList<>();
    private ArrayList<Object> typedArray = new ArrayList<>();
    // missing value handling
    private ArrayList<Boolean> isMissing = new ArrayList<>();   // used to remember missing values even after accounted for
        /* 
            missing pattern is in Dataset. values matching that pattern will get added to isMissing
            values that are unable to be parsed will also be added to isMissing
        */
    public boolean missingOmitted = false; // checks if statistics should omit missing values or use them
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

    public boolean containsMissing() {
        if (isMissing.contains(true)) {
            return true;
        }
        else {
            return false;
        }
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
        // strings don't have missing value handling, so no need to do anything with missing values
        if(newType.equals("String")) {
            for (int i = 0; i < stringArray.size(); i++) {
                typedArray.set(i, stringArray.get(i));
            }
            return true;
        }

        for (int i = 0; i < stringArray.size(); i++) {
            String realType = Dataset.getPattern(stringArray.get(i));
            if(realType.equals("missing")){ 
                // value is missing, properly handled
                typedArray.set(i,null);
                isMissing.set(i,true);
            } else if(!realType.equals(newType)){
                System.out.println("ERROR: " + stringArray.get(i) + " can't be parsed to a " + newType + ". Value set to null");
                // error occurs, but value is handled gracefully by setting to missing
                typedArray.set(i,null);
                isMissing.set(i,true);
            } else if(realType.equals("float")) {
                typedArray.set(i,Float.valueOf(stringArray.get(i)));
                isMissing.set(i,false);
            } else if(realType.equals("boolean")) {
                typedArray.set(i,Boolean.valueOf(stringArray.get(i)));
                isMissing.set(i,false);
            } else {
                System.out.println("ERROR: new valid pattern " + realType + " not added to Field.setType(String newType)");
                isMissing.set(i,true);
                return false;
            }
        }
        return true;
    }


    //Adds a new cell to stringArray and typedArray
    //If the new cell does not match the field type, the index in typedArray is set to null
    //Either way, the cell is added as given to stringArray
    boolean addCell(String newValue) {
        if(type == null && !newValue.isEmpty()) { //if the type hasn't been determined yet and the cell is populated
            System.out.println("ERROR: type not determined");
            return false;
        } 
        //TODO: replace with missing value handling
        if(newValue.isEmpty()) { 
            stringArray.add(newValue);
            typedArray.add(null);
            isMissing.add(true);
            return true;
        }

        //Type is determined and cell is populated
        stringArray.add(newValue);
        String realType = Dataset.getPattern(newValue);
        if (realType.equals("missing")) {
            typedArray.add(null);
            isMissing.add(true);
            return true;
        }
        if(type.equals("String")) {
            typedArray.add(newValue);
        } else {
            if(!type.equals(realType)) {
                System.out.println("ERROR: " + newValue + " is type " + realType + " and cannot be parsed to type " + type);
                typedArray.add(null);
                isMissing.add(true);
                return false;
            }
            if(type.equals("float")) {
                typedArray.add(Float.valueOf(newValue));
                isMissing.add(false);
            } else if(type.equals("boolean")) {
                typedArray.add(Boolean.valueOf(newValue));
                isMissing.add(false);
            } else {
                System.out.println("ERROR: new data type " + realType + " not handled in Field.addCell(String newValue)");
                isMissing.add(true);
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
        isMissing.remove(oldValueIndex);
        return true;
    }

    // handling missing values

    public void handleMissingValues(String method) {
        // to do
        switch(method) {
            case "Replace With Mean":
                replaceMissing(5); // change to mean when done
                missingOmitted = false;
                break;
            case "Replace With Median":
                replaceMissing(5); // change to median when done
                missingOmitted = false;
                break;
            case "Forward Fill":
                forwardFill();
                missingOmitted = false;
                break;
            case "Backward Fill":
                backwardFill();
                missingOmitted = false;
                break;
            case "Omit Missing":
                missingOmitted = true;
                break;
            default:
                throw new IllegalArgumentException("Invalid missing value method: " + method);
        }
    }

    // replaces all missing values with some value
    public void replaceMissing(double value) {
        String stringValue = "" + value;
        for (int i=0; i<typedArray.size(); i++) {
            if(isMissing.get(i)) {
                updateCell(i, stringValue);
                System.out.println("Updated at " + i + " with "+ stringValue);
            }
        }
    }

    // fills all missing values with previous value
    public void forwardFill() {
        // ensure first value is filled by getting from the end of the array
        if(isMissing.get(0)) {
            for(int i=typedArray.size()-1; i>0; i--) {
                if(!isMissing.get(i)) {
                    updateCell(0, stringArray.get(i));
                    break;
                }
            }
        }
        // fill all missing values with previous value
        for (int i=1; i<typedArray.size(); i++) {
            if (isMissing.get(i)) {
                updateCell(i, stringArray.get(i-1));
                System.out.println("Replaced " + stringArray.get(i-1));
            }
        }
    }
    // fills all missing values with next value
    public void backwardFill() {
        // fill final value with first non-missing value
        if(isMissing.get(typedArray.size()-1)) {
            for (int i=0; i<typedArray.size();i++) {
                if(!isMissing.get(i)) {
                    updateCell(typedArray.size(), stringArray.get(i));
                    break;
                }
            }
        }
        // iterate backwards, fill all missing values with index that is 1 greater
        for (int i=typedArray.size()-2; i>0; i--) {
            if (isMissing.get(i)) {
                updateCell(i, stringArray.get(i+1));
            }
        }
    }
}
