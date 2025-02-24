package dataquest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class Dataset {
    public static ArrayList<Field> dataArray = null;

    public Dataset() {
        
    }

    static ArrayList<Field> getDataArray() {
        if(dataArray == null) {
            gui();
        }
        return dataArray;
    }

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

    static int indexOfField(String fieldName){
        for (Field field : dataArray) {
            if(fieldName.equals(field.getName())){
                return dataArray.indexOf(field);
            }
        }
        return -1;
    }
    //TODO: TO BE DELETEED
    //returns String[0] if the field does not exist
    static String[] getValues(String fieldName) {
        if(dataArray == null) {
            gui();
        }
        Field validField=null;
        for (Field field : dataArray) {
            if(fieldName.equals(field.getName())){validField = field;break;}
        }
        try {
            String[] vals = new String[validField.getStringArray().size()];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = validField.getStringArray().get(i);
            }
            return vals;
        } catch (Exception e) {
            System.out.println("ERROR: Field not found");
            for (Field field : dataArray) {
                System.out.println("\t" + field.getName());
            }
            return new String[0];
        }
    }
    static void csvToField(File file) throws IOException{
        BufferedReader csvReader = new BufferedReader(new FileReader(file)); //TODO: fix so less class needs
        String row = csvReader.readLine();
        dataArray = new ArrayList<>();
        String[] rowSplit;
        String[] fieldNames;
        int incorrectCount = 1; //Instantiated with 1 to account for category row
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
                incorrectCount++;
                row = csvReader.readLine();
                continue;
            }
            for (int i = 0; i < rowSplit.length; i++) {
                if(dataArray.get(i).getType() == null){
                    if("true".equalsIgnoreCase(rowSplit[i]) || "false".equalsIgnoreCase(rowSplit[i])) {
                        if(!dataArray.get(i).setType("boolean")){
                            System.out.println("ERROR: Issue setting " + rowSplit[i] + " to type boolean");
                        }
                    }
                    //NOTE: Doesn't separate ints from floats - unnecessary
                    else {
                        try {
                            Float.valueOf(rowSplit[i]);
                            if(!dataArray.get(i).setType("float")){
                                System.out.println("ERROR: Issue setting " + rowSplit[i] + " to type float");
                            }
                        } catch (Exception e) {
                            if(!dataArray.get(i).setType("String")){
                                System.out.println("ERROR: Issue setting " + rowSplit[i] + " to type String");
                            }
                        }
                    }
                }
                if(dataArray.get(i).addCell(rowSplit[i])){
                    continue;
                } else {
                    System.out.println("ERROR: " + rowSplit[i] + " is not a " + dataArray.get(i).getType());
                    incorrectCount++;
                }
            }
            row = csvReader.readLine();
        }
        csvReader.close();
        System.out.println("Reading completed.");
        System.out.println("\tTotal lines of data: " + dataArray.get(0).getStringArray().size());
        System.out.println("\tTotal incorrect lines: " + incorrectCount);
        System.out.println("\tSum: " + (incorrectCount+dataArray.get(0).getStringArray().size()));
    }
    

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
    
    public static void gui(){
        JFrame frame;
        frame = new JFrame("textfield"); 
        frame.setSize(500, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        
        JFileChooser fileSelect = new JFileChooser();
        fileSelect.setAcceptAllFileFilterUsed(false);
        fileSelect.setFileFilter(new FileNameExtensionFilter(".csv, .xls, .xlsx", "csv","xls","xlsx"));
        fileSelect.showOpenDialog(frame);
        File file = new File(fileSelect.getSelectedFile().getAbsolutePath());
        if(file.exists()){
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
        }
        } else {
            System.out.println("Not a valid file type");
        }
    }
}
