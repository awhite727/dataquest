package dataquest;

import java.io.Serializable;
import java.util.ArrayList;

public class Field implements Serializable{
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
    Field(String fieldName) {
        this.name = fieldName;
    }

    // proper way to get numerical data
    ArrayList<Double> getValues() {
        ArrayList<Double> values = new ArrayList<>();
        if(!type.equals("float")) {
            System.out.println("ERROR: Not a float\n\t" + type + "\n\n\n");
            return values;
        }  
        for (int i=0; i<typedArray.size(); i++) {
            Object v = typedArray.get(i);
            // removes nulls as well as handles errors
            // if v is null, v will not be added
            if (v instanceof Number) {     
                values.add(((Number) v).doubleValue());
            }
        }
        return values;
    }
    int getRowCount() {
        return typedArray.size();
    }
    
    void setFieldName(String newName) {
        name = newName;
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

    Object getTypedAtIndex(int index) {
        if(typedArray.size() < index || index < 0) {
            return null;
        }
        return typedArray.get(index);
    }

    public void setName( String name) {
        this.name = name;
    }

    String getCellString(int valIndex){
        if(valIndex >= stringArray.size()){return "ERROR: Index out of bounds";}
        return stringArray.get(valIndex);
    }

    // returns indexes where missing values haven't been handled yet
    public ArrayList<Integer> getMissing() {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i<typedArray.size(); i++) {
            if (typedArray.get(i) == null) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public boolean containsMissing() {
        if (isMissing.contains(true)) {
            return true;
        }
        else {
            return false;
        }
    }
    // returns true if contains 8 or less unique values
    public boolean isCategorical() {
        String [] levels = getLevels();
        if (levels.length > 1) {
            return true;
        }
        return false;
    }
    
    // returns the string of each level in the field, or empty array if there are more than eight levels
    public String[] getLevels() {
        ArrayList<String> levelsList = new ArrayList<>();
        String[] levels = new String[]{};
        switch (type.toLowerCase()) {
            // check for unique, non-empty strings
            case "string":
                for (String s : stringArray) {
                    if (!levelsList.contains(s.strip()) && !s.strip().equals("")) {
                        levelsList.add(s);
                        if (levelsList.size() > 8) {
                            return levels;
                        }
                    }
                }
                break;
            // check if integer, then add unique integers to the levels list
            case "float":
                for (int i=0;i<typedArray.size();i++) {
                    if (typedArray.get(i) instanceof Float) {
                        float value = (float) typedArray.get(i);
                        float valueFloor = (float) Math.floor(value);
                        int realValue;
                        if (Math.abs(value-valueFloor)< 0.0001) {
                            realValue = (int) valueFloor;
                        }
                        else if (Math.abs((valueFloor+1)-value) < 0.0001) {
                            realValue = (int) valueFloor+1;
                        }
                        else {
                            return levels;
                        }
                        String level = realValue + "";
                            if (!levelsList.contains(level)) {
                                levelsList.add(level);
                                if (levelsList.size()>8) {
                                    return levels;
                                }
                            }
                    }
                }
                break;
            // find both cases, then return, or return the singular case
            case "boolean":
                for (int i=0;i<typedArray.size();i++) {
                    if (typedArray.get(i) instanceof Boolean) {
                        boolean value = (boolean) typedArray.get(i);
                        String level = value + "";
                        if(!levelsList.contains(level)) {
                            levelsList.add(level);
                            if (levelsList.size() > 1) {
                                return levelsList.toArray(new String[2]);
                            }
                        }
                    }
                }
                break;
            default:
                Popup.showErrorMessage(null, "Error finding levels of " + name + ": invalid type declared. \n" + name + ": " + type);
        }
        //System.out.println(levelsList.toString());
        levels = levelsList.toArray(new String[levelsList.size()]);
        if (levels.length <= 1) {
            Popup.showErrorMessage(null, "Error finding levels of " + name + ": too few levels to read.");
        }
        return levels;
    }

    // returns all values equal to the string passed
    public ArrayList<Integer> getIndexOfLevel(String level) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i=0;i<stringArray.size(); i++) {
            if (level.equalsIgnoreCase(stringArray.get(i))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    //UPDATED 4/16/2025: now passes boolean saying if all values fit the standard; if there is a cell that doesn't match the type 
        //(i.e. if any cell is a string when the type is being set to float, returns false)
        //Also returns false for other errors, though these should not be reached here and should be properly handled before
        //OLD: 
        //Passed the new type and returns if it succeeds in identifying the type
    //If a cell cannot be set to the new type, the index in typedArray is set to null
    //Case sensitive
    boolean setType(String newType){
        boolean validType = (newType.equals("String") ||newType.equals("float") ||newType.equals("boolean"));
        boolean allTyped = true; 

        //Should not be reached with proper front-end handling
        if(!validType){
            System.out.println("ERROR: Type " + newType + " not recognized");
            //If the type hasn't been set yet, set to String to prevent issues in Dataset
            if(type == null){
                type = "String";
            }
            return false;
        }
        
        type = newType;
        if(stringArray.isEmpty()) return allTyped; 
        //if the newType is a String, there is no need to verify with Patterns
        // strings don't have missing value handling, so no need to do anything with missing values
        if(newType.equals("String")) {
            for (int i = 0; i < stringArray.size(); i++) {
                typedArray.set(i, stringArray.get(i));
            }
            return allTyped;
            //return true;
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
                allTyped = false;
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
        return allTyped;
    }


    //Adds a new cell to stringArray and typedArray
    //If the new cell does not match the field type, the index in typedArray is set to null
    //Either way, the cell is added as given to stringArray
    boolean addCell(String newValue) {
        if(type == null && !newValue.isEmpty()) { //if the type hasn't been determined yet and the cell is populated
            System.out.println("ERROR: type not determined");
            return false;
        } 
        
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
            isMissing.add(false);
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
        if(oldValueIndex > typedArray.size()) return false;

        if(newValue.isEmpty()) { 
            stringArray.set(oldValueIndex, newValue);
            typedArray.set(oldValueIndex, null);
            isMissing.set(oldValueIndex, true);
            return true;
        }
        
        if(type.equals("String")) {
            typedArray.set(oldValueIndex,newValue);
            stringArray.set(oldValueIndex,newValue);
            isMissing.set(oldValueIndex, false);
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
                isMissing.set(oldValueIndex, false);
            } else if(type.equals("boolean")) {
                typedArray.set(oldValueIndex,Boolean.valueOf(newValue));
                stringArray.set(oldValueIndex,newValue);
                isMissing.set(oldValueIndex, false);
            } else {
                System.out.println("ERROR: new data type " + realType + " not handled in Field.updateCell(int oldValueIndex, String newValue)");
                return false;
            }
        }
        return true;
    }

    // for manual entry
    public boolean setCell(int index, String newValue) {
        // if index is exactly at the end of the array, append the new cell.
        String valueType = Dataset.getPattern(newValue);
        if (!type.equals("String") && !valueType.equals(type) && !valueType.equals("missing")) {
            return false;
        }
        if (index == stringArray.size()) {
            return addCell(newValue);
        }
        // if the cell already exists, update it.
        else if (index < stringArray.size()) {
            boolean success = updateCell(index, newValue);
            return success;
        }
        else {
            while (stringArray.size() < index) {
                addCell("");  // add empty cells to fill the gap.
            }
            return addCell(newValue);
        }
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
                omitMissing();  // gets rid of replaced missing values before calculating mean
                double mean = StatisticalSummary.getMean(this.getValues());
                replaceMissing(mean); 
                break;
            case "Replace With Median":
                omitMissing();  // gets rid of replaced missing values before calculating median
                double median = StatisticalSummary.getMedian(this.getValues());
                replaceMissing(median); // change to median when done
                break;
            case "Forward Fill":
                forwardFill();
                break;
            case "Backward Fill":
                backwardFill();
                break;
            case "Omit Missing":
                omitMissing();
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
                typedArray.set(i, value);
                stringArray.set(i, value + "");
                System.out.println("Updated at " + i + " with "+ stringValue);
            }
        }
    }
    public void omitMissing() {
        for (int i=0; i<typedArray.size(); i++) {
            if(isMissing.get(i)) {
                stringArray.set(i, null);
                typedArray.set(i, null);
            }
        }
    }

    // fills all missing values with previous value
    public void forwardFill() {
        // ensure first value is filled by getting from the end of the array
        if(isMissing.get(0)) {
            for(int i=typedArray.size()-1; i>0; i--) {
                if(!isMissing.get(i)) {
                    stringArray.set(0, stringArray.get(i));
                    typedArray.set(0, Float.valueOf(stringArray.get(i)));
                    break;
                }
            }
        }
        // fill all missing values with previous value
        for (int i=1; i<typedArray.size(); i++) {
            if (isMissing.get(i)) {
                stringArray.set(i, stringArray.get(i-1));
                typedArray.set(i, Float.valueOf(stringArray.get(i-1)));
            }
        }
    }
    // fills all missing values with next value
    public void backwardFill() {
        // fill final value with first non-missing value
        if(isMissing.get(typedArray.size()-1)) {
            for (int i=0; i<typedArray.size();i++) {
                if(!isMissing.get(i)) {
                    stringArray.set(stringArray.size()-1, stringArray.get(i));
                    typedArray.set(typedArray.size()-1, Float.valueOf(stringArray.get(i)));
                    break;
                }
            }
        }
        // iterate backwards, fill all missing values with index that is 1 greater
        for (int i=typedArray.size()-2; i>0; i--) {
            if (isMissing.get(i)) {
                stringArray.set(i, stringArray.get(i+1));
                typedArray.set(i, Float.valueOf(stringArray.get(i+1)));
            }
        }
    }
}