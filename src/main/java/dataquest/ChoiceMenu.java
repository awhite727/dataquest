
package dataquest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

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

    // called by other methods, displays a popup with specified choices and returns the results
    // Allowed questionTypes: 
        /* "label" - no specified question; just for display text
         * "combo" - dropdown box 
         * "radio" - bubbles -- returns null if nothing selected
         * "text" - textbox -- returns empty string if nothing added
         * "check" - checkbox (radio but allowing multiple to be selected) -- returns empty string if nothing added
         */
    // for each element in questionList index 0 assumed to be the question name; if none to be used use ""
    // returns String[] of the results in the index; labels are returned as null
    //NOTE: Currently all questions automatically optional
    //If not included it will just return null
    private static String[] showGenericPopup(JFrame parent, String tabName, ArrayList<String> questionType, ArrayList<String[]> questionList) {
        ArrayList<Object> mainComponents = new ArrayList<>();
        ArrayList<Component> allComponents = new ArrayList<>();
        JDialog popup = new JDialog(parent, tabName, true);
        popup.setSize(350, 250);
        //popup.setLayout(new FlowLayout(FlowLayout.LEFT));
        //Note: Could use a GroupLayout to get the vertical and horizontal aligns right??
            //https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html
        //GroupLayout popupLayout = new GroupLayout(popup);
        //popup.setLayout(popupLayout);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        //container.setLayout(new FlowLayout(FlowLayout.LEFT));
        popup.add(container);

        System.out.println("Types: " + questionType);

        for (int i = 0; i < questionType.size(); i++) {
            String qType = questionType.get(i);
            String[] question = questionList.get(i);
            try {
                if(question.length < 1) {
                    mainComponents.add(null);
                    throw new Exception("ERROR: questionList.get("+i+") has a length of " + question.length);
                }
                //If it's a label/textbox it shouldn't have any other elements in the String[] so continue
                //If it's a combo/radio and it only had the title, print error and continue
                if (qType.equals("label")){
                    JLabel tempLabel = new JLabel(question[0]);
                    mainComponents.add(null);
                    allComponents.add(tempLabel);
                    continue;
                } else if(qType.equals("text")){
                    JPanel textPanel = new JPanel();
                    textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    textPanel.add(new JLabel(question[0]));
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS)); // vertical stacking
                    TextField field = new TextField(10);

                    mainComponents.add(field);
                    textPanel.add(field);
                    allComponents.add(textPanel);     
                }
                else if(question.length < 2){ 
                    mainComponents.add(null);
                    throw new Exception ("ERROR: questionList.get("+i+") has length of " + question.length + " and is labelled as a " + qType);
                }
                else if(qType.equals("combo")) {
                    JPanel comboPanel = new JPanel();
                    comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    comboPanel.add(new JLabel(question[0]));
                    comboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    JComboBox<String> comboBox = new JComboBox<>(Arrays.copyOfRange(question, 1, question.length));
                    
                    comboPanel.setBackground(Color.RED); //TODO: Remove, just to show issue for now
                    comboPanel.add(comboBox);
                    mainComponents.add(comboBox);
                    allComponents.add(comboPanel);
                } else if(qType.equals("radio")) {
                    String[] radioOptions = Arrays.copyOfRange(question, 1, question.length);
                    ButtonGroup radioGroup = new ButtonGroup(); 
                    JPanel radioPanel = new JPanel();
                    radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    radioPanel.add(new JLabel(question[0]));
                    JRadioButton[] radioButtons = new JRadioButton[radioOptions.length];
                    radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS)); // vertical stacking
                    for (int j = 0; j < radioOptions.length; j++) {
                        radioButtons[j] = new JRadioButton(radioOptions[j]);
                        radioGroup.add(radioButtons[j]);
                        radioPanel.add(radioButtons[j]);
                    }
                    mainComponents.add(radioButtons);
                    allComponents.add(radioPanel);
                } else if(qType.equals("check")) {
                    String[] checkOptions = Arrays.copyOfRange(question, 1, question.length);
                    JPanel checkPanel = new JPanel();
                    checkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    checkPanel.add(new JLabel(question[0]));
                    
                    JCheckBox[] checkButtons = new JCheckBox[checkOptions.length];
                    
                    checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS)); // vertical stacking
                    for (int j = 0; j < checkOptions.length; j++) {
                        checkButtons[j] = new JCheckBox(checkOptions[j]);
                        checkPanel.add(checkButtons[j]);
                    }
                    mainComponents.add(checkButtons);
                    allComponents.add(checkPanel);            
                } else {
                    throw new Exception("ERROR: Unknown type " + qType);
                }
            } catch (ArrayIndexOutOfBoundsException outError) {
                System.out.println("ERROR: Index out of bounds; Check the values being passed to showGenericPopup");
                outError.printStackTrace();
            } catch (Exception e) { //For generic expected errors
                System.out.println(e);
                e.printStackTrace();
            }
            
        }
        
        //Make confirm button and action listener
        String[] selectedValues = new String[questionType.size()];
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            try {
                for (int i = 0; i < questionType.size(); i++) {
                    String qType = questionType.get(i); 
                    Object box = mainComponents.get(i); //WILL HAVE ERROR? -- 3/26/25 I have no idea what I was meaning about errors here. It seems to be working fine.
                    if (qType.equals("label")) {
                        continue;
                    } else if (qType.equals("combo")) {
                        try {
                            selectedValues[i] = (String) ((JComboBox<String>) box).getSelectedItem();
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + i + ") to a JComboBox<String>");
                        }
                    } else if(qType.equals("radio")) {
                        try {
                            JRadioButton[] radioButtons = (JRadioButton[]) box;
                            for (JRadioButton radioButton : radioButtons) {
                                if (radioButton.isSelected()) {
                                    selectedValues[i] = radioButton.getText();
                                    break;
                                }
                            }
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + i + ") to a JRadioButton[]");
                        }
                    } else if(qType.equals("check")) {
                        try {
                            JCheckBox[] checkButtons = (JCheckBox[]) box;
                            ArrayList<String> checkString = new ArrayList<>();
                            for (JCheckBox checkButton : checkButtons) {
                                if (checkButton.isSelected()) {
                                    checkString.add(checkButton.getText());
                                }
                                selectedValues[i] = String.join(",", checkString);
                            }
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + i + ") to a JCheckBox[]");
                        }
                    } else if(qType.equals("text")) {
                        try {
                            selectedValues[i] = ((TextField)box).getText();
                        } catch (Exception e1) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + i + ") to a TextField");
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException collectError) {
                System.out.println("ERROR: showGenericPopup");
                collectError.printStackTrace();
            }
            popup.dispose();
        });

        allComponents.add(confirmButton);

        System.out.println("Total components: " + allComponents.size());
        for (Component c : allComponents) {
            container.add(c);
            System.out.println("Added: "  + c + "\n");
        }
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
        return selectedValues;
    }


    public static String histogramMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        final String binSize = "Bin Size"; //just to prevent naming errors
        final String numBins = "Number of Bins"; //just to prevent naming errors
        if (fields.length==0) {
            return "No numerical fields available for histogram.";
        }
        String [] fieldNames = new String[fields.length+1];
        fieldNames[0] = "Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldNames[i+1] = fields[i].getName();
        }
        String[] options = new String[]{"Shape Using", binSize, numBins};
        
        //String[] selected = showComboAndRadioPopup(parent, "Histogram", "Field", "Shape Using", fieldNames, options);
        String[] selected = showGenericPopup(parent, "Histogram", new ArrayList<String>(Arrays.asList("combo","radio","text")),new ArrayList<String[]>(Arrays.asList(fieldNames,options, new String[]{"Input: "})));
        System.out.println("Selected size: " + selected.length);
        for (String s : selected) {
            System.out.println("\t" + s);
        }
        if(selected[0] == null || selected[1]==null) return null; //TODO: Add error notification if XOR null
        
        Field summaryField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Histogram histogram = new Histogram(summaryField);
        String histogramOutput = "";
        double textResponse = -1;
        try {
            textResponse = Double.valueOf(selected[2]);
            if(textResponse <= 0) throw new Exception();
            
        } catch (Exception e) {
            //TODO: Add official error notification 
            System.out.println("ERROR: Not a valid text entry. Defaulting to 10 bins");
            return histogram.binFromNum(10); 
        }

        if(selected[1].equals(binSize))
            return histogram.binFromSize(textResponse);
        else if(selected[1].equals(numBins)) {
            try {
                int num = (int)textResponse;
                return histogram.binFromNum(num);
            } catch (Exception e) {
                System.out.println("ERROR: Not a valid text entry. Defaulting to 10 bins");
                return histogram.binFromNum(10);
            }
        } else{ //shouldn't be called but just in case
            histogramOutput = "ERROR: Shape determination not defined. Defaulting to 10 bins\n";
            histogramOutput += histogram.binFromNum(10);
        } 
        return histogramOutput;
   }
}
