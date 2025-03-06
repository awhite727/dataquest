/* 

package dataquest.lib;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVReader {
    public static List<Double> readCSV(String filePath, String columnName) {
        List<Double> data = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                String value = record.get(columnName);
                try {
                    data.add(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    // Ignore non-numeric values
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
} */