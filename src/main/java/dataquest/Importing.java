package dataquest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;

import javax.swing.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;


class Importing {
    
    static JsonArray csvToJSON(File csv) throws IOException {
        JsonArray jsonArray = new JsonArray();
        BufferedReader csvReader = new BufferedReader(new FileReader(csv)); //TODO: fix so less class needs
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
                                stringJson += Boolean.valueOf(cell);
                                if(i+1 < categories.length){stringJson+=",";}
                                continue;                             
                            } else if (datatypes[i].equals("float")){
                                stringJson += Float.valueOf(cell);
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
                                stringJson += Float.valueOf(cell);
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
                    JsonObject o = new JsonParser().parse(stringJson).getAsJsonObject();
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
        System.out.println("Sum: " + (incorrectCount+jsonArray.size()));

        return jsonArray;
    }
    
    public void gui(){
        JTextField textField;
        JFrame frame;
        JButton button;
        frame = new JFrame("textfield");
        button = new JButton("submit");  
        textField = new JTextField(30);
        JPanel panel = new JPanel();
		panel.add(textField);
		panel.add(button);
		frame.add(panel);
		frame.setSize(500, 200);
        frame.setVisible(true);

        button.addActionListener(e -> {
                File file = new File(textField.getText());
                if (file.isFile()){
                    if(file.getName().contains(".csv")) {
                        System.out.println("csv");
                        try {
                            csvToJSON(file);
                        } catch (FileNotFoundException e1) {
                            //Shouldn't be reached by the way this is set up
                            e1.printStackTrace();
                        } catch(IOException e2) {
                            e2.printStackTrace();
                        }
                    } else if(file.getName().contains(".xlsx")) {
                        //TO DO: Handle reading in .xlsx files 
                        System.out.println("xlsx");
                    } else if(file.getName().contains(".xls")) {
                        //TO DO: Handle reading in .xls files
                        System.out.println("xls");
                    } else {
                        System.out.println("Not a valid file type");
                    }
                } else {
                    System.out.println("Not a file");
                }
        });
    }
}
