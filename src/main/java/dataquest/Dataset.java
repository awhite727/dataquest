package dataquest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException; 

class Dataset {
    public static ArrayList<Field> dataArray = null;
    private static Pattern booleanPattern = null;
    private static Pattern numericPattern = null;
    private static Pattern sciNoPattern = null;
    // missing value handling 
    private static Pattern missingPattern = null;

    //Returns the dataArray
    //Checks if the dataArray exists should be handled in Layout
    ArrayList<Field> getDataArray() {
        return dataArray;
    }

    //Sets the dataArray based on a saved workspace
    //Currently only necessary for the way Serialization is handled as of 03/06/2025
    void setDataArray(ArrayList<Field> saved){
        dataArray = new ArrayList<>(saved);
    }

    //Returns a String[] with all of the field names 
    static String[] getFields() {
        String [] fields = new String[dataArray.size()];
        for (int i = 0; i < dataArray.size(); i++) {
            fields[i] = dataArray.get(i).getName();
        }
        return fields;
    }

    // returns all fields with float type
    static Field[] getNumericFields() {
        ArrayList<Field> fieldsList = new ArrayList<>();
        for(int i=0; i<dataArray.size(); i++) {
            Field f = dataArray.get(i);
            if(f.getType() == null) continue;
            if(f.getType().equals("float") && f.getValues().size() > 0) { //prevents choice menu errors where the user set a type to float but there are no actual numbers in that field
                fieldsList.add(f);
            }
        }
        Field[] fields = fieldsList.toArray(new Field[fieldsList.size()]);
        return fields;
    }
    static Field[] getCategoricalFields() {
        ArrayList<Field> fieldsList = new ArrayList<>();
        for (int i=0; i<dataArray.size(); i++) {
            Field f = dataArray.get(i);
            if (f.isCategorical()) {
                fieldsList.add(f);
            }
        }
        Field[] fields = fieldsList.toArray(new Field[fieldsList.size()]);
        return fields;
    }
    // returns all fields with between three and ten levels
    static Field[] getCategoricalAnovaFields() {
        ArrayList<Field> fieldsList = new ArrayList<>();
        for (int i=0; i<dataArray.size(); i++) {
            Field f = dataArray.get(i);
            String[] levels = f.getLevels();
            if (levels.length > 2 && levels.length < 11) {
                fieldsList.add(f);
            }
        }
        return fieldsList.toArray(new Field[fieldsList.size()]);
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

    static int getMaxRowCount() {
        if (dataArray.size() == 0) {
            return 0;
        }
        int max = 0;
        for (int i=0; i<dataArray.size(); i++) {
            if (dataArray.get(i).getRowCount() > max) {
                max = dataArray.get(i).getRowCount();
            }
        }
        return max;
    }

    // takes a list of fields, and outputs a list of list of doubles
    // ensure that all fields passed are numerical
    static ArrayList<ArrayList<Double>> matchFields(Field[] fields) {
        Set<Integer> missingIndex = new HashSet<>();
        for (Field f : fields) {
            if (!f.getType().equalsIgnoreCase("float")) {
                throw new IllegalArgumentException("Fields must be numerical: " + f.getName());
            }
            List<Integer> missing = f.getMissing();    // gets all missing indexes in this field
            missingIndex.addAll(missing);   // adds all new missing indexes
        }
        ArrayList<ArrayList<Double>> values = new ArrayList<>();
        for (Field f: fields) {
            ArrayList<Double> valuesWithoutMissing = new ArrayList<>();
            for (int i=0; i<fields[0].getTypedArray().size(); i++) {
                if (!missingIndex.contains(i)) {    // skips if at least one field has a missing at that value
                    Object value = f.getTypedAtIndex(i);
                    if (value instanceof Number number) {
                        valuesWithoutMissing.add(number.doubleValue());
                    }
                    else {
                        throw new IllegalArgumentException("Values must be numerical: " + f.getName() + "\nValue: " + value + "\nIndex: " + i);
                    }
                }
            }
            values.add(valuesWithoutMissing);
        }
        return values;
    }

    //sets the allowed Patterns and returns an array with the compiled patterns
    static Pattern[] setPatterns(){
            Pattern[] patterns;
            missingPattern = Pattern.compile("^\\s*$|^\\s*(NA|null|n/a|missing|\\?|\\.|-|none|unknown|not available)\\s*$",
                Pattern.CASE_INSENSITIVE);
                        /* 
                            empty strings
                            strings consisting of only whitespace
                            also matches placeholders like na, null, n/a, missing, the characters ?, ., and -, 
                                none, unknown, and not available
                        */
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
        } else if(missingPattern.matcher(cellString).matches()) {
            typeFound = "missing";
        } else if(numericPattern.matcher(cellString).matches()) {
            typeFound = "float";
        } else if(sciNoPattern.matcher(cellString).matches()) {
            typeFound = "float"; //reads the same for Float.valueOf
        } else {
            typeFound = "String";
        }
        return typeFound;
    }

    //Takes the fieldName, creates a Field, and adds it to the Dataset array
    void addManualField(String fieldName){
        dataArray.add(new Field(fieldName));
    }
    
    //Takes in the imported file and fills out the dataArray
    void csvReading(File file, String delim) throws IOException{
        BufferedReader csvReader = new BufferedReader(new FileReader(file));
        String row = csvReader.readLine();
        dataArray = new ArrayList<>();
        String[] rowSplit;
        String[] fieldNames;
        //int incorrectCount = 0;
        if (delim.equals("")){Popup.showErrorMessage(null,"ERROR: No deliminator detected"); csvReader.close(); return;}
        String regex = delim + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        if (row == null) {csvReader.close();return;}
        
        fieldNames = row.split(regex);
        for (String string : fieldNames) {
            dataArray.add(new Field(string));
        }
        row = csvReader.readLine();
        while(row != null) {
            rowSplit = row.split(regex);
            if(rowSplit.length != fieldNames.length) {
                    Popup.showErrorMessage(null, "ERROR: Unable to read row " + row);
                    //incorrectCount++;
                    row = csvReader.readLine();
                    continue;
            }
            for (int i = 0; i < rowSplit.length; i++) {
                //Field.addCell() handles cases where the first row has empty cells
                if(dataArray.get(i).getType() == null){
                    String type = getPattern(rowSplit[i]);
                    if(!type.equals("missing"))
                        dataArray.get(i).setType(type); //Error message printed in Field)
                }
                //attempt to add the cell to the field; if it fails, returns an error 
                if(dataArray.get(i).addCell(rowSplit[i])){
                    continue;
                } else {
                    //incorrectCount++;
                }
            }
            row = csvReader.readLine();
        }
        csvReader.close();
        /* System.out.println("\n~ ~ ~\nReading completed.");
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
        } */
    }
    
    //Takes an imported .xlsx file and fills out the dataArray
    public void xlsxReading(File file) throws IOException{
        //System.out.println("Called");
        XSSFWorkbook wb;
        XSSFSheet sheet;
        DataFormatter df = new DataFormatter(); //have emulatecsv = true if we want to prevent trimming
        df.setUseCachedValuesForFormulaCells(true);
        String type = "";
        dataArray = new ArrayList<>();
        //int incorrectCount = 0;
        try {
            //System.out.println("Attempt to get sheet");
            wb = new XSSFWorkbook(file);
            sheet = wb.getSheetAt(0);
            if(sheet == null){
                Popup.showErrorMessage(null, "ERROR: Sheet not found within file");
                wb.close();
                return;
            }
            wb.close();
        } catch(InvalidFormatException ioException) {
            Popup.showErrorMessage(null,"ERROR: Not a valid file type");
            return;
        }
        Row fields = sheet.getRow(sheet.getFirstRowNum()); //updated to find the first populated row, not just the first row
        //System.out.println("Preparing iterations: ");
        int numFields = fields.getPhysicalNumberOfCells();
        for (Row row : sheet){
            for (int j=0; j < numFields; j++) {
                Cell cell = row.getCell(j);
                if(cell == null && row.equals(fields)) {
                    //System.out.println("Unnamed column identified"); //for testing
                    dataArray.add(new Field(""));
                    continue;
                }
                String cellString = df.formatCellValue(cell);
                if(row.equals(fields)) {
                    if(!cell.getCellType().toString().equalsIgnoreCase("STRING")){
                        System.out.println("NOTICE: Cell " + df.formatCellValue(cell) + " is not a String. Is this intended to be a data cell?");
                    }
                    dataArray.add(new Field(cellString));
                    continue;
                } 
                if(dataArray.get(j).getType() == null){
                    type = getPattern(cellString);
                    if(!type.equals("missing"))
                        dataArray.get(j).setType(type); //Error message printed in Field
                }
                if(!dataArray.get(j).addCell(cellString)){
                    //incorrectCount++;
                }
            }
        }
        //Check for edge cases of empty columns/only named columns
        /* ArrayList<Field> deleteFields = new ArrayList<>();
        for (Field field : dataArray) {
            if(field.getType() != null) continue;
            else if(field.getName().strip().equals("")) deleteFields.add(field);
            else field.setType("String");
            System.out.println(field.getName());
        }
        for (Field field : deleteFields) {
            dataArray.ind
        }*/
        
        /* System.out.println("\n~ ~ ~\nReading completed.");
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
        } */
    }
    
    //Takes an imported .xls file and fills out the dataArray
    public void xlsReading(File file) throws IOException{
        Workbook workbook;
        Sheet sheet;
        String type = "";
        dataArray = new ArrayList<>();
        //int incorrectCount = 0;

        try {
            //System.out.println("Attempt to get sheet");
            workbook = Workbook.getWorkbook(file);
            sheet = workbook.getSheet(0);
            int numFields = sheet.getColumns();
            int numRows = sheet.getRows();
            for (int i=0; i < numFields; i++){
                for(int j=0; j < numRows; j++){
                    String cellString = sheet.getCell(i, j).getContents();
                    if(j==0){
                        dataArray.add(new Field(cellString));
                        continue;
                    }
                    if(dataArray.get(i).getType() == null){
                        type = getPattern(cellString);
                        if(!type.equals("missing"))
                            dataArray.get(i).setType(type); //Error message printed in Field
                    }
                    if(!dataArray.get(i).addCell(cellString)){
                        //++;
                    }
                }              
            }
            workbook.close();
            //System.out.println("Success!");
        } catch (BiffException | IOException e) {
            System.out.println("ERROR: Issue reading a BIFF file");
            e.printStackTrace();
            return;
        }
        /* System.out.println("\n~ ~ ~\nReading completed.");
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
        } */
    }

    public static void trimDataArray() {
        if (dataArray == null) return;
        dataArray.removeIf(field -> field.getType() == null && field.getName().strip().equals("")); //remove unused columns (sometimes slips through if data used to be there and was deleted, especially for xlsx)
        dataArray.stream().filter(field -> field.getType()==null).forEach(field -> {
                    if(field.getName().strip().equals("")) field.setType("String"); //Prevent any null type slipping through (occurs if there are columns with names but no data)
        });
    }
}
