package dataquest;

import java.util.ArrayList;

public class Field {
    private String name;
    private String type = null;
    private ArrayList<String> stringArray = new ArrayList<>();
    private ArrayList<Object> typedArray = new ArrayList<>();
    Field(String fieldName) {//, ArrayList<String> fieldArray){
        this.name = fieldName;
        
    }
    Field(String fieldName, String fieldType) {
        this.name = fieldName;
        boolean validType = (fieldType.equalsIgnoreCase("String") ||fieldType.equalsIgnoreCase("float") ||fieldType.equalsIgnoreCase("boolean"));
        if(validType){
            this.type = fieldType;
        }else {
            this.type = "String";
        }
    }
    Field(String fieldName, String fieldType, ArrayList<String> fieldArray){
        this.name = fieldName;
        this.stringArray = fieldArray;
        boolean validType = (fieldType.equalsIgnoreCase("String") ||fieldType.equalsIgnoreCase("float") ||fieldType.equalsIgnoreCase("boolean"));
        if(validType){
            this.type = fieldType;
            setType(fieldType);
        }else {
            this.type = "String";
            setType(fieldType);
        }
    }
    boolean setType(String newType){
        boolean validType = (newType.equalsIgnoreCase("String") ||newType.equalsIgnoreCase("float") ||newType.equalsIgnoreCase("boolean"));
        if(!validType){
            System.out.println("ERROR: Type " + newType + " not recognized");
            return false;
        } else if(stringArray.isEmpty()) {
            if(newType.equalsIgnoreCase("String")) {type = "String";}
            else if(newType.equalsIgnoreCase("float")) {type = "float";}
            else if(newType.equalsIgnoreCase("boolean")) {type = "boolean";}
            return true;
        }
        if(newType.equalsIgnoreCase("String")) {
            type = "String";
            for (int i = 0; i < stringArray.size(); i++) {
                if(typedArray.size() < i){
                    typedArray.add(stringArray.get(i));
                } else {
                    typedArray.set(i, stringArray.get(i));
                }
            }
        } else if (newType.equalsIgnoreCase("float")) {
            type = "float";
            for (int i = 0; i < stringArray.size(); i++) {
                try {
                    if(typedArray.size() < i){
                        typedArray.add(Float.valueOf(stringArray.get(i)));
                    } else {
                        typedArray.set(i,Float.valueOf(stringArray.get(i)));
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + stringArray.get(i) + " can't be parsed to a float. Value set to null");
                    if(typedArray.size() < i){
                        typedArray.add(null);
                    } else {
                        typedArray.set(i,null);
                    }
                }
            }
        } else if (newType.equalsIgnoreCase("boolean")) {
            type = "boolean";
            for (int i = 0; i < stringArray.size(); i++) {
                try {
                    if(typedArray.size() < i){
                        typedArray.add(Boolean.valueOf(stringArray.get(i)));
                    } else {
                        typedArray.set(i,Boolean.valueOf(stringArray.get(i)));
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + stringArray.get(i) + " can't be parsed to a boolean. Value set to null");
                    if(typedArray.size() < i){
                        typedArray.add(null);
                    } else {
                        typedArray.set(i,null);
                    }
                }
            }
        } 
        return true;
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

    boolean addCell(String newValue) {
        stringArray.add(newValue);
        if(type.equals("String")) {
            typedArray.add(newValue);
        } else if(type.equals("float")) {
            try {
                typedArray.add(Float.valueOf(newValue));
            } catch (Exception e) {
                System.out.println("ERROR: cannot parse " + newValue + " to float");
                typedArray.add(null);
                return false;
            }
        } else if(type.equals("boolean")) {
            try {
                typedArray.add(Boolean.valueOf(newValue));
            } catch (Exception e) {
                System.out.println("ERROR: cannot parse " + newValue + " to boolean");
                typedArray.add(null);
                return false;
            }
        } else {
            System.out.println("ERROR: Unknown data type: " + type);
            typedArray.add(null);
            return false;
        }
        return true;
    }

    String getCellString(int valIndex){
        if(valIndex >= stringArray.size()){return "ERROR: Index out of bounds";}
        return stringArray.get(valIndex);
    }

    boolean updateCell(int oldValueIndex, String newValue){
        if(oldValueIndex > typedArray.size()){return false;}
        if(type.equals("String")) {
            stringArray.set(oldValueIndex,newValue);
            typedArray.set(oldValueIndex,newValue);
        } else if(type.equals("float")) {
            try {
                typedArray.set(oldValueIndex,Float.valueOf(newValue));
                stringArray.set(oldValueIndex,newValue);
            } catch (Exception e) {
                System.out.println("ERROR: cannot parse " + newValue + " to float");
                return false;
            }
        } else if(type.equals("boolean")) {
            try {
                typedArray.set(oldValueIndex,Boolean.valueOf(newValue));
                stringArray.set(oldValueIndex,newValue);
            } catch (Exception e) {
                System.out.println("ERROR: cannot parse " + newValue + " to boolean");
                return false;
            }
        } else {
            System.out.println("ERROR: Unknown data type: " + type);
            typedArray.add(null);
            return false;
        }
        return true;
    }

    boolean deleteCell(int oldValueIndex){
        if(oldValueIndex > stringArray.size()){return false;}
        stringArray.remove(oldValueIndex);
        typedArray.remove(oldValueIndex);
        return true;
    }
}
