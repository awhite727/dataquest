package dataquest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

//NOTE: apache.poi and JXL share some import names 
//If more imports are needed later and share the same class name, 
//Replace class name in declarations to full import name (i.e. "jxl.Sheet sheet = new jxl.Sheet()")
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//NOTE: JXL has some known vulnerabilities, including with SQL injection
//Not relevant to our current plan, but be careful using it outside of local usage
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException; 

class Dataset {
    public static ArrayList<Field> dataArray = null;
    private static Pattern booleanPattern = null;
    private static Pattern numericPattern = null;
    private static Pattern sciNoPattern = null;

    //Checks if the dataArray exists, and if not opens the importing window, then returns the dataArray
    ArrayList<Field> getDataArray() {
        if(dataArray == null) {
            gui();
        }
        return dataArray;
    }

    //Returns a String[] with all of the field names 
    String[] getFields() {
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
    //TODO: Not called by Dataset or Field; delete if viewing elements does not need 
    int indexOfField(String fieldName){
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

    //Takes in the imported file and fills out the dataArray
    void csvToField(File file) throws IOException{
        BufferedReader csvReader = new BufferedReader(new FileReader(file));
        String row = csvReader.readLine();
        dataArray = new ArrayList<>();
        String[] rowSplit;
        String[] fieldNames;
        int incorrectCount = 0; 
        if (row == null) {csvReader.close();return;}
        
        fieldNames = row.split(",");
        for (String string : fieldNames) {
            dataArray.add(new Field(string));
        }
        row = csvReader.readLine();
        while(row != null) {
            rowSplit = row.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if(rowSplit.length != fieldNames.length) {
                    System.out.println("ERROR: Unable to unpack row " + row);
                    incorrectCount++;
                    row = csvReader.readLine();
                    continue;
            }
            for (int i = 0; i < rowSplit.length; i++) {
                //Field.addCell() handles cases where the first row has empty cells
                if(dataArray.get(i).getType() == null && !rowSplit[i].strip().isEmpty()){
                    String type = getPattern(rowSplit[i].strip());
                    dataArray.get(i).setType(type); //Error message printed in Field)
                }
                //attempt to add the cell to the field; if it fails, returns an error 
                if(dataArray.get(i).addCell(rowSplit[i].strip())){
                    continue;
                } else {
                    incorrectCount++;
                }
            }
            row = csvReader.readLine();
        }
        csvReader.close();
        System.out.println("\n~ ~ ~\nReading completed.");
        System.out.println("Total lines of data: " + dataArray.get(0).getStringArray().size());
        System.out.println("Lines with typing errors: " + incorrectCount);
        for (Field field : dataArray) {
            int nullCount = 0;
            for (int i=0; i < field.getTypedArray().size(); i++) {
                if(field.getTypedArray().get(i) == null && !field.getStringArray().get(i).isEmpty()) {
                    nullCount++;
                }
            } 
            if(nullCount > 0) {
                System.out.println("\t" + field.getName() + ": " + nullCount);
            }
        }
    }
    
    //Takes an imported .xlsx file and fills out the dataArray
    public void xlsxReading(File file) throws IOException{
        XSSFWorkbook wb;
        XSSFSheet sheet;
        DataFormatter df = new DataFormatter();
        String type = "";
        dataArray = new ArrayList<>();
        int incorrectCount = 0;
        try {
            System.out.println("Attempt to get sheet");
            wb = new XSSFWorkbook(file);
            sheet = wb.getSheetAt(0);
            if(sheet == null){
                System.out.println("ERROR: Sheet not found within file");
                wb.close();
                return;
            }
            wb.close();
        } catch(InvalidFormatException ioException) {
            System.out.println("ERROR: Not a valid file type");
            return;
        }
        Row fields = sheet.getRow(0);
        System.out.println("Preparing iterations: ");
        int numFields = fields.getPhysicalNumberOfCells();
        for (Row row : sheet){
            for (int j=0; j < numFields; j++) {
                Cell cell = row.getCell(j);
                String cellString = df.formatCellValue(cell).strip();
                if(row.equals(fields)) {
                    if(!cell.getCellType().toString().equalsIgnoreCase("STRING")){
                        System.out.println("NOTICE: Cell " + df.formatCellValue(cell) + "is not a String. Is this intended to be a data cell?");
                    }
                    dataArray.add(new Field(cellString));
                    continue;
                } 
                if(dataArray.get(j).getType() == null && !cellString.isEmpty()){
                    type = getPattern(cellString);
                    dataArray.get(j).setType(type); //Error message printed in Field
                }
                if(!dataArray.get(j).addCell(cellString)){
                    incorrectCount++;
                }
            }
        }
        System.out.println("\n~ ~ ~\nReading completed.");
        System.out.println("Total lines of data: " + dataArray.get(0).getStringArray().size());
        System.out.println("Lines with typing errors: " + incorrectCount);
        for (Field field : dataArray) {
            int nullCount = 0;
            for (int i=0; i < field.getTypedArray().size(); i++) {
                if(field.getTypedArray().get(i) == null && !field.getStringArray().get(i).isEmpty()) {
                    nullCount++;
                }
            } 
            if(nullCount > 0) {
                System.out.println("\t" + field.getName() + ": " + nullCount);
            }
        }
    }
    
    //Takes an imported .xls file and fills out the dataArray
    public void xlsReading(File file) throws IOException{
        Workbook workbook;
        Sheet sheet;
        String type = "";
        dataArray = new ArrayList<>();
        int incorrectCount = 0;

        try {
            System.out.println("Attempt to get sheet");
            workbook = Workbook.getWorkbook(file);
            sheet = workbook.getSheet(0);
            int numFields = sheet.getColumns();
            int numRows = sheet.getRows();
            for (int i=0; i < numFields; i++){
                for(int j=0; j < numRows; j++){
                    String cellString = sheet.getCell(i, j).getContents().strip();
                    if(j==0){
                        dataArray.add(new Field(cellString));
                        continue;
                    }
                    if(dataArray.get(i).getType() == null && !cellString.isEmpty()){
                        type = getPattern(cellString);
                        dataArray.get(i).setType(type); //Error message printed in Field
                    }
                    if(!dataArray.get(i).addCell(cellString)){
                        incorrectCount++;
                    }
                }              
            }
            workbook.close();
            System.out.println("Success!");
        } catch (BiffException | IOException e) {
            System.out.println("ERROR: Issue reading a BIFF file");
            e.printStackTrace();
            return;
        }
        System.out.println("\n~ ~ ~\nReading completed.");
        System.out.println("Total lines of data: " + dataArray.get(0).getStringArray().size());
        System.out.println("Lines with typing errors: " + incorrectCount);
        for (Field field : dataArray) {
            int nullCount = 0;
            for (int i=0; i < field.getTypedArray().size(); i++) {
                if(field.getTypedArray().get(i) == null && !field.getStringArray().get(i).isEmpty()) {
                    nullCount++;
                }
            } 
            if(nullCount > 0) {
                System.out.println("\t" + field.getName() + ": " + nullCount);
            }
        }
    }
    
    //Called by gui()
    //Calls PythonAssist.py to borrow its improved directory for importing
   //Returns a File if it is a valid File, returns null if not
   private File importingWithPy(){
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

    //Sets up a basic gui and pops up the importing window 
    //Calls the repesective methods for unpacking a csv, xls, or xlsx
    //TODO: Lock button so it cannot be pressed multiple times? 
    //Doesn't currently cause any issues other than multiple importing windows opening
    //But could potentially cause issues later
   public void gui() {
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
               if(file.getName().equals("")){}//nothing selected
               else if(file.getName().endsWith(".csv")) {
                  System.out.println("csv");
                  csvToField(file); //TODO: change name to csvReading to match naming scheme
               } else if(file.getName().endsWith(".xlsx")) {
                  System.out.println("xlsx");
                  xlsxReading(file);
               } else if(file.getName().endsWith(".xls")) {
                  System.out.println("xls");
                  xlsReading(file);
               } else {
                  System.out.println("Not a valid file type: " + file.getName());
               }
            } catch(IOException e) {
               System.out.println("File not found: ");
               System.out.println(file);
            } catch(Exception e) {
                System.out.println("ERROR: Unknown error in Dataset.gui()");
                e.printStackTrace();
            }
      }});





      
    }

    public static void main(String[] args) {
        Dataset d = new Dataset();
        d.gui();
    }
}
