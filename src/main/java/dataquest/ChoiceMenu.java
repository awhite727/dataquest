package src.main.java.dataquest;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dataquest.CSVReader;
import dataquest.ExcelReader;
import main.java.dataquest.StatisticalSummary;

public class ChoiceMenu extends JFrame {
    private JButton statsButton;

    public ChoiceMenu() {
        // Set up the main window
        setTitle("Main Window");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Create the Statistical Summary button
        statsButton = new JButton("Statistical Summary");
        statsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatisticalSummary();
            }
        });
        add(statsButton);
    }

    private void showStatisticalSummary() {
        // Prompt the user to select a file
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();

        // Read data from the file
        List<Double> data;
        if (filePath.endsWith(".csv")) {
            data = CSVReader.readCSV(filePath, "ColumnName"); // Replace "ColumnName" with the actual column name
        } else if (filePath.endsWith(".xlsx") || filePath.endsWith(".xls")) {
            data = ExcelReader.readExcel(filePath, 0, 0); // Replace 0 with the actual sheet and column index
        } else {
            JOptionPane.showMessageDialog(this, "Unsupported file format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if the dataset is empty
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No valid data found in the file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the statistical summary
        String summary = StatisticalSummary.getSummary(data);

        // Display the summary in a popup
        JOptionPane.showMessageDialog(this, summary, "Statistical Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChoiceMenu().setVisible(true);
            }
        });
    }
}