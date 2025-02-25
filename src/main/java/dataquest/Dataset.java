package dataquest;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class Dataset {
    public static ArrayList<Field> dataArray = null;
    private static Pattern booleanPattern = null;
    private static Pattern numericPattern = null;
    private static Pattern sciNoPattern = null;

    //Checks if the dataArray exists, and if not opens the importing window, then returns the dataArray
    static ArrayList<Field> getDataArray() {
        if(dataArray == null) {
            gui();
        }
        return dataArray;
    }

    //Returns a String[] with all of the field names 
    static String[] getFields() {
        if(dataArray == null) {
            gui();
        }
        String [] fields = new String[dataArray.size()];
        for (int i = 0; i < dataArray.size(); i++) {
            fields[i] = dataArray.get(i).getName();
        }
        return fields;
    }

    //Takes the fieldName and returns the index within dataArray; returns -1 if the field does not exist
    static int indexOfField(String fieldName){
        for (Field field : dataArray) {
            if(fieldName.equals(field.getName())){
                return dataArray.indexOf(field);
            }
        }
        return -1;
    }

    //sets the allowed Patterns and returns an array with the compiled patterns
    static Pattern[] setPatterns(){
            Pattern[] patterns;
            booleanPattern = Pattern.compile("^true|false$", Pattern.CASE_INSENSITIVE);
            numericPattern = Pattern.compile("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$");
                        /* Start with an optional +|-
                        End with a mandatory: 
                            At least one number, followed by an optional
                            decimal with any number of numbers following
                            OR exactly one decimal with 1 or more numbers following (without number before)
                        Example allowed edges: 
                            +.1
                            1. */
            sciNoPattern = Pattern.compile("^[+-]?([1-9]([.][0-9]+)?[e][-]?[0-9]+)$", Pattern.CASE_INSENSITIVE);
                        /*Start with an optional +|-
                        End with a mandatory: 
                            Exactly 1 number 1-9, 
                                optionally (exactly one decimal and at least one number 0-9 after) 
                            Exactly 1 'e'
                            optionally one - 
                            And at least one number after afterwards
                        Example tests: 
                            String test = "+1E-1"; //Sci
                            test = "1.333e5"; //Sci
                            test = "12.1e1"; //String
                            test = "1.e1"; //String
                            test = "1e"; //String
                            test = "1e.1"; //String
                            test = "4.321768E3"; //Sci
                        */   
            patterns = new Pattern[]{booleanPattern,numericPattern,sciNoPattern};
            return patterns;
        }

    //Takes a cell value as input and returns the pattern it matches
    static String getPattern(String cellString) {
        String typeFound = "";
            //Check if the patterns have been compiled (no need to check all) and if not compile them
            if(booleanPattern == null) {
                setPatterns();
            }
        
        //use patterns to find and return the type
        if(booleanPattern.matcher(cellString).matches()) {
            typeFound = "boolean";
        } else if(numericPattern.matcher(cellString).matches()) {
            typeFound = "float";
        } else if(sciNoPattern.matcher(cellString).matches()) {
            typeFound = "float"; //reads the same for Float.valueOf
        } else {
            typeFound = "String";
        }
        return typeFound;
    }

    //Helps csvToField split complex CSVs 
    //TODO: combine with csvToField after thorough testing
    private static String[] unpackCSVwithComma(String row, int numFields) {
        String[] unpacked;// = new String[numFields];
        //Copied from https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes 
        String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"; 
            //Split by ,
                //That has a __ after it (?=(__)) of any amount
                    //Don't split by anything (?:) that matches the substring
                        //Any amount of characters not a " 
                        //followed by a quote
                        //folowed by any amount of characters not a "
                        //followed by another " 
                //Ending in any amount of any character not a "
            
        unpacked = row.split(regex);
        if (unpacked.length != numFields) {
            unpacked = new String[0];
        }
        return unpacked;
    }

    //Takes in the imported file and fills out the dataArray
    static void csvToField(File file) throws IOException{
        BufferedReader csvReader = new BufferedReader(new FileReader(file)); //TODO: fix so less class needs
        String row = csvReader.readLine();
        dataArray = new ArrayList<>();
        String[] rowSplit;
        String[] fieldNames;
        int incorrectCount = 0; 
        int commaSplit = 0;
        if (row == null) {csvReader.close();return;}
        
        fieldNames = row.split(",");
        for (String string : fieldNames) {
            dataArray.add(new Field(string));
        }
        row = csvReader.readLine();
        while(row != null) {
            rowSplit = row.split(",");
            if(rowSplit.length > fieldNames.length) {
                //System.out.println("ERROR: Skipping data with a comma within it");
                rowSplit = unpackCSVwithComma(row, fieldNames.length);
                if(rowSplit.length == 0) {
                    System.out.println("ERROR: Unable to unpack row " + row);
                    incorrectCount++;
                    row = csvReader.readLine();
                    continue;
                } else {
                    //System.out.println("\nrowSplit updated: " + Arrays.toString(rowSplit));
                    commaSplit++;
                }
            }
            for (int i = 0; i < rowSplit.length; i++) {
                if(dataArray.get(i).getType() == null){
                    String type = getPattern(rowSplit[i].strip());
                    dataArray.get(i).setType(type); //Error message printed in Field
                }
                //attempt to add the cell to the field; if it fails, returns an error 
                if(dataArray.get(i).addCell(rowSplit[i].strip())){
                    continue;
                } else {
                    System.out.println("ERROR: " + rowSplit[i].strip() + " is not a " + dataArray.get(i).getType());
                    incorrectCount++;
                }
            }
            row = csvReader.readLine();
        }
        csvReader.close();
        System.out.println("Reading completed.");
        System.out.println("\tTotal lines of data: " + dataArray.get(0).getStringArray().size());
        System.out.println("\tTotal incorrect lines: " + incorrectCount);
        System.out.println("\tSuccessful comma splits: " + commaSplit);
        System.out.println("\tSum: " + (incorrectCount+dataArray.get(0).getStringArray().size())+1);
    }
    
    //Takes an imported file and fills out the dataArray
    static void xlsxReading(File file) throws IOException{
        int incorrectCount = 1; //Instantiated with 1 to account for category row
        //POIFSFileSystem fs;
        XSSFWorkbook wb;
        XSSFSheet sheet;
        try {
            //fs = new POIFSFileSystem(file);
            wb = new XSSFWorkbook(file);
            sheet = wb.getSheetAt(0);
            wb.close();
            //HSSFRow row = sheet.getRow(0);
        } catch(Exception ioException) {
            ioException.printStackTrace();
            return;
        }
        int numFields = sheet.getRow(0).getPhysicalNumberOfCells();
        for (Row row : sheet) {
            if(dataArray == null) {
                dataArray = new ArrayList<>();
                for (Cell cell : row) {
                    dataArray.add(new Field(cell.getStringCellValue()));
                    //NOTE: getStringCellValue throws an error if it's numeric or formulas
                }
                continue;
            }
            else {
                for (int i=0; i < numFields; i++) {
                    String cell = "";
                    try {
                        cell = row.getCell(i).getStringCellValue();
                    } catch (Exception e1) {
                        try {
                            cell = row.getCell(i).getBooleanCellValue()+"";
                        } catch (Exception e2) {
                            cell = (row.getCell(i).getNumericCellValue())+"";
                        //NOTE: getNumericCellValue is a double, not float
                        }
                        
                    }
                    if(dataArray.get(i).getType() == null){
                        if("true".equalsIgnoreCase(cell) || "false".equalsIgnoreCase(cell)) {
                            if(!dataArray.get(i).setType("boolean")){
                                System.out.println("ERROR: Issue setting " + cell + " to type boolean");
                            }
                        }
                        //NOTE: Doesn't separate ints from floats - unnecessary
                        else {
                            try {
                                Float.valueOf(cell);
                                if(!dataArray.get(i).setType("float")){
                                    System.out.println("ERROR: Issue setting " + cell + " to type float");
                                }
                            } catch (Exception e) {
                                if(!dataArray.get(i).setType("String")){
                                    System.out.println("ERROR: Issue setting " + cell + " to type String");
                                }
                            }
                        }
                    }
                    if(dataArray.get(i).addCell(cell)){
                        continue;
                    } else {
                        System.out.println("ERROR: " + cell + " is not a " + dataArray.get(i).getType());
                        incorrectCount++;
                    }
                }
            }
        }
        System.out.println("Reading completed.");
        System.out.println("\tTotal lines of data: " + dataArray.get(0).getStringArray().size());
        System.out.println("\tTotal incorrect lines: " + incorrectCount);
        System.out.println("\tSum: " + (incorrectCount+dataArray.get(0).getStringArray().size()));        
    }
    
    //Sets up a basic gui and pops up the importing window 
    //Calls the repesective methods for unpacking a csv, xls, or xlsx
    public static void gui(){
        JFrame frame;
        JFileChooser fileSelect;
        File file;

        frame = new JFrame("textfield"); 
        frame.setSize(500, 200);
        frame.setVisible(true);
        //frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            System.out.println("Error loading system LookAndFeel: Using swing basic");
        }
        fileSelect = new JFileChooser();
        fileSelect.setAcceptAllFileFilterUsed(false);
        fileSelect.setFileFilter(new FileNameExtensionFilter(".csv, .xls, .xlsx", "csv","xls","xlsx"));
        fileSelect.showOpenDialog(frame);
        try{
            file = new File(fileSelect.getSelectedFile().getAbsolutePath());
        }
        catch (NullPointerException e){
            return;
        }
        if(file.getName().endsWith(".csv")) {
            System.out.println("csv");
            try {
                csvToField(file);
            } catch(IOException e1) {
                System.out.println("CSV File not found");
            }
        } else if(file.getName().endsWith(".xlsx")) {
            System.out.println("xlsx");
            try {
                xlsxReading(file);
            } catch(IOException e1) {
                System.out.println("xlsx File not found");
            }
        } else if(file.getName().endsWith(".xls")) {
            //TODO: Handle reading in .xls files
            System.out.println("xls");
        } else {
            System.out.println("Not a valid file type");
        }
    }
    //Calls PythonAssist.py to borrow its improved directory for importing
   //Returns a File if it is a valid File, returns null if not 
   private static File importingWithPy(){
      String pythonPath = "src\\main\\resources\\PythonAssist.py";
      String selectedPath = "";
      File file = null;
      ProcessBuilder pb = new ProcessBuilder()
         .command("python","-u", pythonPath, "openFile");
      Process p;

      try {
         //run the process from process builder; 
         p = pb.start();
         BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
         selectedPath = in.readLine();
         p.waitFor();
         in.close();
         file = new File(selectedPath);
      } catch (IOException e) {
         System.out.println("ERROR: " + pythonPath + " could not be found");
         e.printStackTrace();
      } catch (InterruptedException e) {
         //Process p interupted by another thread
         e.printStackTrace();
      } catch (NullPointerException e){
         //file selection canceled 
      }
     
      return file;
   }
   public static void newGui() {
      JFrame frame = new JFrame("textfield"); 
      JPanel panel = new JPanel();
      JButton button = new JButton("Import");
      panel.add(button);
      frame.add(panel);
      frame.setSize(500, 200);
      frame.setVisible(true);
      frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
      
      button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            File file = null;
            try {
               file = importingWithPy();
               if(file.getName().endsWith(".csv")) {
                  System.out.println("csv");
                  csvToField(file);
               } else if(file.getName().endsWith(".xlsx")) {
                  System.out.println("xlsx");
                  xlsxReading(file);
               } else if(file.getName().endsWith(".xls")) {
                  //TODO: Handle reading in .xls files
                  System.out.println("xls");
               } else {
                  System.out.println("Not a valid file type");
               }
            } catch (NullPointerException e) {
               //Error or nothing selected; errors handled in importingWithPy
            } catch(IOException e) {
               System.out.println("xlsx File not found");
            }

      }});
    }
    public static void main(String[] args) {
        newGui();
    }
}
