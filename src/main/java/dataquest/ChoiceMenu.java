
package dataquest;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ChoiceMenu {

    /* menu methods are statically called from layout
        new tests should have their own method that passes prompts and questions to a pop-up method
        methods that make pop-ups are private to encourage code reusability
    */

   public static String statisticalSummaryMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        if (fields.length==0) {
            return "No numerical fields available for statistical summary.";
        }
        String [] fieldNames = new String[fields.length];
        for (int i=0;i<fields.length;i++) {
            fieldNames[i] = fields[i].getName();
        }
        String selected = showComboPopup(parent, "Statistical Summary", "Choose a field", fieldNames);
        Field summaryField = Dataset.dataArray.get(Dataset.indexOfField(selected));
        String textOutput = StatisticalSummary.getSummary(summaryField.getValues());
        return textOutput;
   }

    // menu for handling missing values
    public static void missingValueMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        ArrayList<String> fieldNamesList = new ArrayList<>();
        for (Field f:fields) {
            if (f.containsMissing()) {
                fieldNamesList.add(f.getName());
            }
        }
        // no missing to handle
        if (fieldNamesList.isEmpty()) {
            // output notification to terminal
            System.out.println("No missing to handle.");
            return;
        }
        String[] fieldNames = fieldNamesList.toArray(new String[fieldNamesList.size()]);
        String[] options = new String[]{"Replace With Mean", "Replace With Median", "Forward Fill", "Backward Fill", "Omit Missing"};

        String[] selected = showComboAndRadioPopup(parent, "Handle Missing Values", "Field", "Handle By", fieldNames, options);
        String missingFieldName = selected[0];
        String missingMethod = selected[1];

        Field missingField = Dataset.dataArray.get(Dataset.indexOfField(missingFieldName));
        missingField.handleMissingValues(missingMethod);
    }

    public static String linearRegression(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        if (fields.length <= 1) {
            return "Not enough numeric fields to perform linear regression.";
        }
        String [] fieldNames = new String[fields.length];
        for (int i=0; i<fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        // pass to choice menu
        String targetName = "";
        String[] parameterNames = new String[2];
        // get fields
        Field target = Dataset.dataArray.get(Dataset.indexOfField(targetName));
        Field[] parameters = new Field[parameterNames.length];
        for (int i =0; i<parameterNames.length; i++) {
            parameters[i] = Dataset.dataArray.get(Dataset.indexOfField(parameterNames[i]));
        }

        String output = StatisticalSummary.getLinearRegression(target, parameters);
        return output;
    }

    public static Boxplot boxplotMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        Field[] levels = Dataset.getCategoricalFields();
        if (fields.length==0) {
            System.out.println("No numerical fields to display.");
            return null;
        }
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i<fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        String boxplotFieldName;
        Field boxplotField;
        Field categoryField = null;
        if (levels.length==0) {
            boxplotFieldName = showComboPopup(parent, "Boxplot", "Choose a field", fieldNames);
        }
        else {
            String[] levelNames = new String[levels.length + 1];
            levelNames[0] = "None";
            for (int i = 1; i<levels.length+1; i++) {
                levelNames[i] = levels[i-1].getName();
            }
            String[] selected = showTwoComboPopup(parent, "Boxplot", "Choose field for boxplot", 
                "(Optional) Choose field for categories", fieldNames, levelNames);
            boxplotFieldName = selected[0];
            if (selected[1].equalsIgnoreCase("none")) {
                categoryField = null;
            }
            else {
                categoryField = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
            }
        }
        boxplotField = Dataset.dataArray.get(Dataset.indexOfField(boxplotFieldName));
        Boxplot boxplot = new Boxplot("Boxplot", null, boxplotField, categoryField);
        return boxplot;
    }

    // called by other methods for simple combo box choices
    // pass the layout, the title, the subtitle, and the options
    // returns the choice selected as a string
    // combo box is generally for choosing a field
    private static String showComboPopup(JFrame parent, String title, String subtitle, String[] options) {
        JDialog popup = new JDialog(parent, "Choice Menu", true);
        popup.setSize(300, 200);
        popup.setLayout(new FlowLayout());
        
        JLabel titleLabel = new JLabel(title);
        JLabel subtitleLabel = new JLabel(subtitle);
        popup.add(titleLabel);
        popup.add(subtitleLabel);

        // check for this before passing to this method, or code will probably crash
        if (options.length == 0) {
            System.out.println("Error creating pop-up: no combo options to display.");
            return "";
        }
        JComboBox<String> comboBox = new JComboBox<>(options);
        popup.add(comboBox);
        
        final String[] selectedOption = {null};
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            selectedOption[0] = (String) comboBox.getSelectedItem();
            popup.dispose();
        });
        popup.add(confirmButton);
        
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
        return selectedOption[0];
    }

    private static String[] showTwoComboPopup(JFrame parent, String title, String label1, String label2, String[] options1, String[] options2) {
        JDialog popup = new JDialog(parent, "Choice Menu", true);
        popup.setSize(300, 200);
        popup.setLayout(new FlowLayout());
        
        JLabel titleLabel = new JLabel(title);
        popup.add(titleLabel);

        // check for this before passing to this method, or code will probably crash
        if (options1.length == 0 || options2.length==0) {
            System.out.println("Error creating pop-up: no combo options to display.");
            return null;
        }
        JComboBox<String> comboBox1 = new JComboBox<>(options1);
        JComboBox<String> comboBox2 = new JComboBox<>(options2);
        JLabel comboLabel1 = new JLabel(label1);
        JLabel comboLabel2 = new JLabel(label2);
        popup.add(comboBox1);
        popup.add(comboLabel1);
        popup.add(comboBox2);
        popup.add(comboLabel2);
        
        String[] selectedOptions = {null, null};
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            selectedOptions[0] = (String) comboBox1.getSelectedItem();
            selectedOptions[1] = (String) comboBox2.getSelectedItem();
            popup.dispose();
        });
        popup.add(confirmButton);
        
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
        return selectedOptions;
        }

    // called by other methods, displays a combo box and a radio option and returns the chosen of both
    // of the return, index 0 is combo choice and index 1 is radio choice
    // combo box is generally for choosing a field
    // radio is generally for choosing what to do with the field
    private static String[] showComboAndRadioPopup(JFrame parent, String title, String comboQuestion, String radioQuestion, String[] comboOptions, String[] radioOptions) {
        JDialog popup = new JDialog(parent, "Choice & Radio Menu", true);
        popup.setSize(350, 250);
        popup.setLayout(new FlowLayout());
        
        JLabel titleLabel = new JLabel(title);
        popup.add(titleLabel);
        
        // check for this before passing to this method, or something will probably crash
        if (comboOptions.length == 0 || radioOptions.length == 0) {
            System.out.println("Error creating pop-up: no combo box or radio button options to display.");
        }

        JLabel comboLabel = new JLabel(comboQuestion);
        JComboBox<String> comboBox = new JComboBox<>(comboOptions);
        popup.add(comboLabel);
        popup.add(comboBox);
        
        ButtonGroup radioGroup = new ButtonGroup();
        JPanel radioPanel = new JPanel();
        JRadioButton[] radioButtons = new JRadioButton[radioOptions.length];
        JLabel radioLabel = new JLabel(radioQuestion);
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS)); // vertical stacking
        radioPanel.add(radioLabel);
        for (int i = 0; i < radioOptions.length; i++) {
            radioButtons[i] = new JRadioButton(radioOptions[i]);
            radioGroup.add(radioButtons[i]);
            radioButtons[i].setAlignmentX(Component.RIGHT_ALIGNMENT); // align to the right so buttons are in a line
            radioPanel.add(radioButtons[i]);
        }
        popup.add(radioPanel);
        
        final String[] selectedValues = {null, null};
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            selectedValues[0] = (String) comboBox.getSelectedItem();
            for (JRadioButton radioButton : radioButtons) {
                if (radioButton.isSelected()) {
                    selectedValues[1] = radioButton.getText();
                    break;
                }
            }
            popup.dispose();
        });
        popup.add(confirmButton);
        
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
        return selectedValues;
    }
}
