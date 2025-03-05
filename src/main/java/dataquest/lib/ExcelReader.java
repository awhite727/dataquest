package dataquest;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook; // For .xls files
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For .xlsx files

public class ExcelReader {
    public static List<Double> readExcel(String filePath, int sheetIndex, int columnIndex) {
        List<Double> data = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(new File(filePath))) {
            Workbook workbook;
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file); // For .xlsx files
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(file); // For .xls files
            } else {
                throw new IllegalArgumentException("Unsupported file format. Only .xlsx and .xls are supported.");
            }

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            for (Row row : sheet) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            data.add(cell.getNumericCellValue());
                            break;
                        case STRING:
                            try {
                                data.add(Double.parseDouble(cell.getStringCellValue()));
                            } catch (NumberFormatException e) {
                                // Ignore non-numeric values
                            }
                            break;
                        default:
                            // Ignore other cell types
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}