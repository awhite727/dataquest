package dataquest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import com.google.gson.*;

class Importing {
    static void readFirstJSON(JsonArray jsonArray) {
        System.out.println("Read check: " + jsonArray.get(0).getAsJsonObject().toString());
    }
    
    static JsonArray csvToJSON(String fileString) throws IOException {
        JsonArray jsonArray = new JsonArray();
        BufferedReader csvReader = new BufferedReader(new FileReader(fileString)); //TODO: fix so less class needs
        String row = csvReader.readLine();
        String[] categories;
        String[] datatypes;
        String[] rowSplit;
        String stringJson = "";
        int incorrectCount = 1; //Instantiated with 1 to account for category row
        if (row != null) {
            categories = row.split(",");
            datatypes = new String[categories.length];

            //TODO: Handle when the first String category (i.e. date or a title) appears to be number/boolean
            //TODO: Include date datatype
            //NOTE: Currently handles mismatched types by printing error and adding it as a String
            row = csvReader.readLine();
            while(row != null) {
                rowSplit = row.split(",");
                if(rowSplit.length > categories.length) {
                    System.out.println("ERROR: Skipping data with a comma within it");
                    incorrectCount++;
                    row = csvReader.readLine();
                    continue;
                } else {
                    stringJson = "{"; //Start building csv into a parseable string
                    for (int i = 0; i < rowSplit.length; i++) {
                        stringJson += "\"" + categories[i] +"\": ";
                        String cell = rowSplit[i];
                        //TODO: Implement null handling (JSON doesn't allow?)
                        if (cell == null || cell.isEmpty()) {
                            stringJson += "\"NA\"";
                            if(i+1 < categories.length){stringJson+=",";}
                            continue;
                        }
                        //datatypes[] used to keep datatypes consistent down the whole category
                        if(datatypes[i] != null){
                            if(datatypes[i].equals("boolean")){
                                try {
                                    stringJson += Boolean.valueOf(cell);
                                } catch (Exception e) {
                                    System.out.println("Mismatched type error: " + cell + " is not a boolean");
                                    stringJson += "\""+cell+"\"";
                                    incorrectCount++;
                                }
                                if(i+1 < categories.length){stringJson+=",";}
                                continue;                                 
                            } else if (datatypes[i].equals("float")){
                                try {
                                    stringJson += Float.valueOf(cell);
                                } catch (Exception e) {
                                    System.out.println("Mismatched type error: " + cell + " is not a number");
                                    stringJson += "\""+cell+"\"";
                                    incorrectCount++;
                                }
                                if(i+1 < categories.length){stringJson+=",";}
                                continue;
                            } else if(datatypes[i].equals("String")){
                                stringJson += "\"" + cell + "\"";
                                if(i+1 < categories.length){stringJson+=",";}
                                continue;
                            } else {
                                System.out.println("ERROR: Unknown data type: " + datatypes[i]);
                                incorrectCount++;
                                continue;
                            }
                        } else if("true".equalsIgnoreCase(cell) || "false".equalsIgnoreCase(cell)) {
                            datatypes[i] = "boolean";
                            stringJson += Boolean.valueOf(cell);
                            if(i+1 < categories.length){stringJson+=",";}
                            continue;                         
                        }
                        //NOTE: Doesn't separate ints from floats - unnecessary
                        else {
                            try {
                                // if(cell.contains("F") || cell.contains("f")) {
                                //     throw new Exception(""); //catches when the first String of a category contains F
                                // }
                                stringJson += Float.valueOf(cell);
                                System.out.println("\tFloat: " + cell);
                                datatypes[i] = "float";
                                if(i+1 < categories.length){stringJson+=",";}
                                continue;
                            } catch (Exception e) {
                                datatypes[i] = "String";
                                stringJson += "\"" + cell + "\"";
                                if(i+1 < categories.length){stringJson+=",";}
                                continue;
                            }
                        }                
                    }
                }
                stringJson += "}";
                try {
                    JsonObject o = JsonParser.parseString(stringJson).getAsJsonObject();
                    jsonArray.add(o);
                    //System.out.println("Attempt: " + o.toString());
                } catch (Exception e) {
                    System.out.println("ERROR: Malformed data; data skipped");
                    incorrectCount++;
                }
                row = csvReader.readLine();

            }
        }
        csvReader.close();
        System.out.println("Reading completed.");
        System.out.println("\tTotal lines of data: " + jsonArray.size());
        System.out.println("\tTotal incorrect lines: " + incorrectCount);
        System.out.println("\tSum: " + (incorrectCount+jsonArray.size()));

        return jsonArray;
    }
    
    public void gui(){
        JFrame frame;
        frame = new JFrame("textfield"); 
        frame.setSize(500, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        
        JFileChooser fileSelect = new JFileChooser();
        fileSelect.setAcceptAllFileFilterUsed(false);
        fileSelect.setFileFilter(new FileNameExtensionFilter(".csv, .xls, .xlsx", "csv","xls","xlsx"));
        fileSelect.showOpenDialog(frame);
        String fileString = fileSelect.getSelectedFile().getAbsolutePath();
        if(fileString.endsWith(".csv")){
            System.out.println("csv");
            try {
                readFirstJSON(csvToJSON(fileString));
            } catch(IOException e1) {
                System.out.println("CSV File not found");
            }
        } else if(fileString.endsWith(".xlsx")) {
            //TODO: Handle reading in .xlsx files 
            System.out.println("xlsx");
        } else if(fileString.endsWith(".xls")) {
            //TODO: Handle reading in .xls files
            System.out.println("xls");
        } else {
            System.out.println("Not a valid file type");
        }
        //});
    }
}
