
package dataquest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.poi.hpsf.Array;
import org.apache.poi.xwpf.usermodel.BreakClear;

public class ChoiceMenu {

    /* menu methods are statically called from layout
        new tests should have their own method that passes prompts and questions to a pop-up method
        methods that make pop-ups are private to encourage code reusability
    */
    public static String importingDelimMenu(JFrame parent) {
        final ArrayList<String> questionType = new ArrayList<String>(Arrays.asList("radio","text"));
        ArrayList<String[]> questionList = new ArrayList<String[]>(2);
        questionList.add(new String[]{"File deliminator: ","Comma", "Semicolon", "Space", "New line", "Tab","Custom"});
        questionList.add(new String[]{""});
        String[] selected=  Popup.showGenericPopup(parent, "Deliminator", questionType, 
        questionList, new ArrayList<>());
        if(selected[0] == null) return null;
        if((!selected[0].equals("Custom")) && selected[1].length() > 0) {
            System.out.println("ERROR: Custom deliminator used but " + selected[0] + " deliminator selected.");
            System.out.println("\t" + selected[1].length());
            return null;
        }
        switch (selected[0]) {
            case "Comma":
                return ",";
            case "Semicolon":
                return ";";
            case "Space":
                return " ";
            case "New line":
                return "\n";
            case "Tab":
                return "\t";
            case "Custom":
                if(selected[1].length() < 1) {
                    System.out.println("ERROR: No custom deliminator assigned.");
                    return null;
                }
                else if(selected[1].length() > 1) {
                    System.out.println("NOTICE: Your custom deliminator is made of more than 1 character and will therefore only separate on an exact match of the full string.");
                    return selected[1];
                }
            default:
                System.out.println("ERROR: Unexpected error in importingDelimMenu");
                return null;
        }
    }

    // set to void to avoid compilation errors, will return Visualization once finished.
    public static Visualization visualMenu(JFrame parent) {
        String[] visualOptions = {"Choose a visualization: ", "Boxplot", "Histogram", "Scatterplot", "T-Distribution"};

        String tabName = "Visualization";
        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("radio"));
        ArrayList<String[]> questionList = new ArrayList<>();
        questionList.add(visualOptions);

        String[] selected = Popup.showGenericPopup(parent, tabName, questionType, questionList, new ArrayList<>()); //Popup.genericPopup is more up to date
        Visualization output;
        switch (selected[0]) {
            case "Boxplot":
                if (Dataset.dataArray != null) {
                    output = boxplotMenu(parent);
                    break;
                }
            case "Scatterplot":
                if (Dataset.dataArray!=null) {
                    output = scatterplotMenu(parent);
                    break;
                }
            case "T-Distribution":
                output = tDistributionMenu(parent);
                break;
            case "Histogram":
                output = histogramMenu(parent);
                break;
            default:
                System.out.println("Error creating visualization of type: " + selected[0]);
                return null;
        }
        return output;
    }

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
        String textOutput = StatisticalSummary.getSummary(summaryField);
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
        String[] radioFieldNames = new String[fields.length + 1];
        String[] checkFieldNames = new String[fields.length + 1];
        radioFieldNames[0] = "Target field: ";
        checkFieldNames[0] = "Predictors: ";
        for (int i=0; i<fields.length; i++) {
            String name = fields[i].getName();
            radioFieldNames[i+1] = name;
            checkFieldNames[i+1] = name;
        }

        // pass to choice menu
        String tabName = "Linear Regression";
        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("radio", "check"));
        ArrayList<String[]> questionList = new ArrayList<>(Arrays.asList(radioFieldNames, checkFieldNames));
        
        String[] selected = Popup.showGenericPopup(parent, tabName, questionType, questionList, new ArrayList<String[]>());//showGenericPopup(parent, tabName, questionType, questionList);
        if (selected.length <= 1) {
            return "Invalid selection";
        }
        String targetName = selected[0];
        String[] parameterNames = selected[1].split(",");
        
        
        // get field
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

    public static Graph scatterplotMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            System.out.println("Not enough numeric fields");
            return null;
        }
        Field[] categoryFields = Dataset.getCategoricalFields();
        String[] xFieldNames = new String[fields.length + 1];
        String[] yFieldNames = new String[fields.length + 1];
        xFieldNames[0] = "X Variable";
        yFieldNames[1] = "Y Variable";
        for (int i = 0; i<fields.length; i++) {
            String name = fields[i].getName();
            xFieldNames[i + 1] = name;
            yFieldNames[i + 1] = name;
        }
        String [] categoryFieldNames = new String[categoryFields.length + 2];
        categoryFieldNames[0] = "(Optional) Level";
        categoryFieldNames[1] = "none";
        for (int i = 0; i< categoryFields.length; i++) {
            categoryFieldNames[i+2] = categoryFields[i].getName();
        }
        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("combo", "combo", "combo"));
        ArrayList<String[]> questionList = new ArrayList<>(Arrays.asList(xFieldNames, yFieldNames, categoryFieldNames));
        String[] selected = Popup.showGenericPopup(parent, "Scatterplot", questionType, questionList, new ArrayList<String[]>());

        //String[] selected = showTwoComboPopup(parent, "Scatterplot", "X Variable", "Y Variable", fieldNames, fieldNames);
        Field xField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field yField = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        String categoryFieldName = selected[2];
        Graph graph;
        if (categoryFieldName.equals("none")) {
            graph = new Graph("Scatterplot", null, xField, yField, null);
        }
        else {
            int index = Dataset.indexOfField(categoryFieldName);
            if (index != -1) {
                Field categoryField = Dataset.dataArray.get(index);
                graph = new Graph("Scatterplot", null, xField, yField, categoryField);
            }
            else {
                System.out.println("Error finding field of name " + categoryFieldName);
                graph = new Graph("Scatterplot", null, xField, yField, null);
            }
        }
        return graph;
    }

    public static TDistribution tDistributionMenu(JFrame parent) {

        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("text", "radio", "text"));
        String [] dfQuestion = new String[] {"Degrees of Freedom"};
        String [] directionQuestion = new String[] {"Direction", "Upper Tail", "Lower Tail", "Two-Tailed", "Confidence Interval"};
        String [] aQuestion = new String[] {"T-Score"};
        ArrayList<String[]> questionList = new ArrayList<>(Arrays.asList(dfQuestion, directionQuestion, aQuestion));

        String[] selected = Popup.showGenericPopup(parent, "T-Distribution", questionType, questionList, new ArrayList<String[]>());

        int df = 10;
        double a  = 0;
        try {
            df = Integer.parseInt(selected[0]);
            a = Double.parseDouble(selected[2]);
        } catch(NumberFormatException e) {
            System.out.println("Invalid entry: " + e.getMessage());
            df = 10;
            a = 0;
        }
        String selectedDirection = selected[1];
        String direction = "inside";
        switch (selectedDirection) {
            case "Upper Tail":
                direction = ">";
                break;
            case "Lower Tail":
                direction = "<";
                break;
            case "Two-Tailed":
                direction = "outside";
                break;
            case "Confidence Interval":
                direction = "inside";
                break;
        }

        TDistribution tDistribution = new TDistribution(null, null, df, direction, a);
        return tDistribution;
    }

    public static String anovaMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        Field[] levels = Dataset.getCategoricalAnovaFields();
        if (fields.length==0) {
            return "No numerical fields for calculation";
        }
        if (levels.length == 0) {
            return "No categorical fields for splitting";
        }
        // case where the numerical and categorical field are the same field
        if (fields.length == 1 && levels.length == 1 && fields[0].getName().equals(levels[0].getName())) {
            return "Not enough fields for calculation";
        }
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i<fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        String[] levelNames = new String[levels.length];
        for (int i=0; i<levels.length; i++) {
            levelNames[i] = levels[i].getName();
        }
        String[] selected = showTwoComboPopup(parent, "ANOVA", "Test field", "Level", fieldNames, levelNames);
        Field numField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field levField = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        return StatisticalSummary.getAnova(numField, levField);
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
    /* private static String[] showGenericPopup(JFrame parent, String tabName, ArrayList<String> questionType, ArrayList<String[]> questionList) {
        ArrayList<Object> mainComponents = new ArrayList<>();
        ArrayList<Component> allComponents = new ArrayList<>();
        JDialog popup = new JDialog(parent, tabName, true);
        Dimension minSize = new Dimension(350, 250);
        popup.setMinimumSize(minSize);
        //popup.setLayout(new FlowLayout(FlowLayout.LEFT));
        //Note: Could use a GroupLayout to get the vertical and horizontal aligns right??
            //https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html
        //GroupLayout popupLayout = new GroupLayout(popup);
        //popup.setLayout(popupLayout);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
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
        popup.pack();
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
        return selectedValues;
    } */

    //TODO: Add official error notifications 
    public static Histogram histogramMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        final String binSize; //just to prevent naming errors
        final String numBins; //just to prevent naming errors
        if (fields.length==0) {
            System.out.println("No numerical fields available for histogram.");
            return null;
        }
        //preparing for generic popup
        binSize = "Bin Size";
        numBins = "Number of Bins";
        String [] fieldNames = new String[fields.length+1];
        fieldNames[0] = "Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldNames[i+1] = fields[i].getName();
        }
        String[] shapeBy = new String[]{"Shape Using: ", binSize, numBins};
        ArrayList<String[]> errors = new ArrayList<>();

        String[] selected = Popup.showGenericPopup(parent, "Histogram",
            new ArrayList<String>(Arrays.asList("combo","radio","text")), 
            new ArrayList<String[]>(Arrays.asList(fieldNames,shapeBy, new String[]{"Input: "})),
            errors);
        System.out.println("Selected size: " + selected.length);
        for (String s : selected) {
            System.out.println("\t" + s);
        }
        if(selected[0] == null || selected[0].equals("") || selected[1]==null) return null; //TODO: Add error notification if XOR null
        
        Field summaryField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Histogram histogram = new Histogram("TEMP Histogram", null, summaryField);
        double textResponse = -1;
        try {
            textResponse = Double.valueOf(selected[2]);
            if(textResponse <= 0) throw new Exception();
            
        } catch (Exception e) {
            System.out.println("ERROR: Not a valid text entry. Defaulting to 10 bins");
            histogram.setBins(true,10);
            return histogram;
        }

        if(selected[1].equals(binSize)) {
            histogram.setBins(false,textResponse);
        } else if(selected[1].equals(numBins)) {
            try {
                int num = (int)textResponse;
                histogram.setBins(true,num);
            } catch (Exception e) {
                System.out.println("ERROR: Not valid: Assuming size");
                histogram.setBins(false,textResponse);
            }
        } else { //shouldn't be called but just in case
            System.out.println("ERROR: Shape using undefined");
        } 
        return histogram;
   }

   //NOTE: Easiest way to handle difference and hnull
   //is to have boolean twoSided
   //and if false, change the difference/order passing Fields to match the onesided 
   //Makes a lot of checks much easier 
    public static String tTwoSampleMenu(JFrame parent) {
        final String tabName = "Two Sample T-Test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            System.out.println("ERROR: Not enough numerical fields available for mean comparison.");
            return null;
        }
        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<String[]> errors = new ArrayList<>(0);
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        String [] fieldB = new String[fields.length+1];
        fieldA[0] = "First Field: ";
        fieldB[0] = "Second Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
            fieldB[i+1] = fields[i].getName();
        } //TODO: note that tabbedPane might be handy for popups or something maybe for like the graph menus? 

        //Fields
        questionType.add("combo");
        questionList.add(fieldA);
        questionType.add("combo");
        questionList.add(fieldB);

        //Hypothesis 
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        questionType.add("comparison");
        questionList.add(new String[]{"Field A - Field B    ","<",">","=","    d"}); //\u2260 is uncode for !=         
        questionType.add("text");
        questionList.add(new String[]{"d = "});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("radio");
        questionList.add(new String[]{"Assume equal variance?","yes (Student/Pooled t-test)", "no (Welch t-test)"});       
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);

        //Check validity, if anything null return 
        /* for (String string : selected) {
            if (string == null) return "";
        } */
        System.out.println(Arrays.toString(selected));
        if(selected[0] == "" || selected[1] == "" || selected[3] == "" || selected[9] == null) {
            System.out.println("ERROR: Nulls");
            return "";
        }
        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        String directionCheck = selected[3];
        double difference = 0; //selected[4]
        double alpha = 0.05; //selected[7]
        String test = selected[9];
        Direction direction;
        switch (directionCheck) {
            case "<":
                direction = Direction.LESS_THAN;
                break;
            case ">":
                direction = Direction.GREATER_THAN;
                break;
            case "=":
                direction = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + directionCheck);
                return "";
        }
        if(!selected[4].strip().equals("")) {
            try {
                difference = Double.parseDouble(selected[4]); //valueOf? 

            } catch (Exception e) {
                System.out.println("ERROR: d must be a number. Using default 0");
            }
        }
        if(!selected[7].strip().equals("")) {
            try {
                alpha = Double.parseDouble(selected[4]); //valueOf? 
                if(alpha <= 0 || alpha >= 1) {
                    alpha = 0.05;
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("ERROR: alpha \u03B1 must be a number 0-1 (exclusive). Using default 0.05");
            }
        }
        TwoSample tt = new TwoSample(fieldOne, fieldTwo, alpha, difference, direction);
        if(test.equals("yes (Student/Pooled t-test)")) {
            tt.setPooled();
        } else if(test.equals("no (Welch t-test)")) {
            tt.setWelch();
        } else {
            System.out.println("ERROR: Test not found");
            return "";
        }
        return tt.printBasic();
    }

    public static String pairedMenu(JFrame parent) {
        final String tabName = "Paired Sample T-Test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            System.out.println("ERROR: Not enough numerical fields available for mean comparison.");
            return null;
        }
        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<String[]> errors = new ArrayList<>(0);
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        String [] fieldB = new String[fields.length+1];
        fieldA[0] = "First Field: ";
        fieldB[0] = "Second Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
            fieldB[i+1] = fields[i].getName();
        } //TODO: note that tabbedPane might be handy for popups or something maybe for like the graph menus? 

        //Fields
        questionType.add("combo");
        questionList.add(fieldA);
        questionType.add("combo");
        questionList.add(fieldB);

        //Hypothesis 
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        questionType.add("comparison");
        questionList.add(new String[]{"Field A - Field B    ","<",">","=","    d"}); //\u2260 is uncode for !=         
        questionType.add("text");
        questionList.add(new String[]{"d = "});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);

        //Check validity, if anything null return 
        /* for (String string : selected) {
            if (string == null) return "";
        } */
        System.out.println(Arrays.toString(selected));
        if(selected[0] == "" || selected[1] == "" || selected[3] == "") {
            System.out.println("ERROR: Nulls");
            return "";
        }
        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        String directionCheck = selected[3];
        double difference = 0; //selected[4]
        double alpha = 0.05; //selected[7]

        Direction direction;
        switch (directionCheck) {
            case "<":
                direction = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                direction = Direction.GREATER_THAN;
                break;
            case "=":
                direction = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + directionCheck);
                return "";
        }
        if(!selected[4].strip().equals("")) {
            try {
                difference = Double.parseDouble(selected[4]); //valueOf? 

            } catch (Exception e) {
                System.out.println("ERROR: d must be a number. Using default 0");
            }
        }
        if(!selected[7].strip().equals("")) {
            try {
                alpha = Double.parseDouble(selected[4]); //valueOf? 
                if(alpha <= 0 || alpha >= 1) {
                    alpha = 0.05;
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("ERROR: alpha \u03B1 must be a number 0-1 (exclusive). Using default 0.05");
            }
        }
        TwoSample tt = new TwoSample(fieldOne, fieldTwo, alpha, difference, direction);
        tt.setPaired();
        return tt.printBasic();
    }

    //TODO: COMPLETE POPUP
    public static String zTwoSampleMenu(JFrame parent) {
        final String tabName = "Two Sample Z-Test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            System.out.println("ERROR: Not enough numerical fields available for mean comparison.");
            return "";
        }
        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<String[]> errors = new ArrayList<>(0);
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        String [] fieldB = new String[fields.length+1];
        fieldA[0] = "First Field: ";
        fieldB[0] = "Second Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
            fieldB[i+1] = fields[i].getName();
        }

        //Fields
        questionType.add("combo");
        questionList.add(fieldA);
        questionType.add("combo");
        questionList.add(fieldB);

        //Get SDs 
        questionType.add("text");
        questionList.add(new String[]{"Field A \u03C3 = "}); //population sd sigma
        questionType.add("text");
        questionList.add(new String[]{"Field B \u03C3 = "}); //population sd sigma
        //TODO: If they don't input, say to use t-test if unknown
        
        //Hypothesis 
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        questionType.add("comparison");
        questionList.add(new String[]{"Field A - Field B    ","<",">","=","    d"}); //\u2260 is uncode for !=         
        
        questionType.add("text");
        questionList.add(new String[]{"d = "});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);

        //Check validity, if anything null return 
        /* for (String string : selected) {
            if (string == null) return "";
        } */
        System.out.println(Arrays.toString(selected));
        if(selected[0] == "" || selected[1] == "" || selected[6] =="") {
            System.out.println("ERROR: Nulls");
            return "";
        } else if (selected[2] == "" || selected[3] == "") {
            System.out.println("ERROR: Population SDs required for a z-test. If unknown, try a two sample t-test");
        }
        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        double sdA = -1; //selected[2]
        double sdB = -1; //selected[3]
        String directionCheck = selected[6];
        double difference = 0; //selected[7]
        double alpha = 0.05; // selected[10];
        Direction direction;
        try {
            sdA = Double.parseDouble(selected[2]); //valueOf? 
            sdB = Double.parseDouble(selected[3]); //valueOf?
        } catch (Exception e) {
            System.out.println("ERROR: SDs must be numbers.");
            return "";
        }

        switch (directionCheck) {
            case "<":
                direction = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                direction = Direction.GREATER_THAN;
                break;
            case "=":
                direction = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + directionCheck);
                return "";
        }
        if(!selected[7].strip().equals("")) {
            try {
                difference = Double.parseDouble(selected[7]); //valueOf? 

            } catch (Exception e) {
                System.out.println("ERROR: d must be a number. Using default 0");
            }
        }
        if(!selected[10].strip().equals("")) {
            try {
                alpha = Double.parseDouble(selected[10]); //valueOf? 
                if(alpha <= 0 || alpha >= 1) {
                    alpha = 0.05;
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("ERROR: alpha \u03B1 must be a number 0-1 (exclusive). Using default 0.05");
            }
        }

        TwoSample tt = new TwoSample(fieldOne, fieldTwo, sdA, sdB, alpha, difference, direction);
        tt.setZ();
        return tt.printBasic();
    }

    public static String proportionMenu(JFrame parent) {
        final String tabName = "Proportion z-test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 1) {
            System.out.println("ERROR: No numerical fields available.");
            return "";
        } else if(fields.length < 2) {
            System.out.println("NOTE: Not enough numerical fields available for a two-proportion test.");
            return oneProportionMenu(parent);
        }
        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<String[]> errors = new ArrayList<>(0);
        questionType.add("radio");
        questionList.add(new String[]{"What type of proportion test would you like to perform?", "One-Sample", "Two-Sample"});

        String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);
        if(selected[0] == null) return "";
        else if(selected[0].equals("One-Sample")) return oneProportionMenu(parent);
        else if(selected[0].equals("Two-Sample")) return twoProportionMenu(parent);
        else return "";
    }

    public static String oneProportionMenu(JFrame parent) {
        final String tabName = "One Sample Proportion";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 1) {
            System.out.println("ERROR: No numerical fields available.");
            return "";
        }

        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<String[]> errors = new ArrayList<>(0);
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        fieldA[0] = "Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
        }

        //Fields and success checkers
        questionType.add("combo");
        questionList.add(fieldA); 
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("label");
        questionList.add(new String[]{"Success parameters"});
        questionType.add("comparison");
        questionList.add(new String[]{"x    ","<",">","=","    success"}); //\u2260 is uncode for !=         
        questionType.add("text");
        questionList.add(new String[]{"success = "});

        
        //Hypothesis
        questionType.add("label");
        questionList.add(new String[]{"\n"}); 
        questionType.add("label");
        questionList.add(new String[]{"Hypothesis"});
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        questionType.add("comparison");
        questionList.add(new String[]{"\u0070\u0302    ","<",">","=","    P\u2080"}); //phat <>= PNull //\u2260 is uncode for !=        
        
        questionType.add("text");
        questionList.add(new String[]{"P\u2080 = "}); // Pnull
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "}); // alpha
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);


        System.out.println(Arrays.toString(selected));
        if(selected[0] == "" || selected[3] == "" || selected[4] == "" || selected[8] == "" || selected[9] == "") {
            System.out.println("ERROR: Nulls");
            return "";
        }
        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        String successDirectionCheck = selected[3];
        double successBound = -1; //selected[4];
        String nullDirectionCheck = selected[8];
        double pNull = -1; //selected[9]
        double alpha = 0.05; //selected[11]
        
        Direction successDirection;
        switch (successDirectionCheck) {
            case "<":
                successDirection = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                successDirection = Direction.GREATER_THAN;
                break;
            case "=":
                successDirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + successDirectionCheck);
                return "";
        }
        try {
            successBound = Double.parseDouble(selected[4]); //valueOf? 
        } catch (Exception e) {
            System.out.println("ERROR: success bound must be a number.");
            return "";
        }

        Direction nullDirection;
        switch (nullDirectionCheck) {
            case "<":
                nullDirection = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                nullDirection = Direction.GREATER_THAN;
                break;
            case "=":
                nullDirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + nullDirectionCheck);
                return "";
        }
        
        if(!selected[9].strip().equals("")) {
            try {
                pNull = Double.parseDouble(selected[9]); //valueOf? 
                if(pNull < 0 || pNull > 1) throw new Exception();
            } catch (Exception e) {
                System.out.println("ERROR: P\u2080 must be a number 0-1.");
                return "";
            }
        }
        

        if(!selected[11].strip().equals("")) {
            try {
                alpha = Double.parseDouble(selected[11]); //valueOf? 
                if(alpha <= 0 || alpha >= 1) {
                    alpha = 0.05;
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("ERROR: alpha \u03B1 must be a number 0-1 (exclusive). Using default 0.05");
            }
        }
        Proportion proportion = new Proportion(fieldOne, successBound, successDirection, alpha, pNull, nullDirection);
        proportion.setOneProportion();
        return proportion.printBasic();
    }
    public static String twoProportionMenu(JFrame parent) {
        final String tabName = "Proportion z-test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            System.out.println("ERROR: Not enough numerical fields available.");
            return "";
        }

        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<String[]> errors = new ArrayList<>(0);
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        String [] fieldB = new String[fields.length+1];

        fieldA[0] = "Field A: ";
        fieldB[0] = "Field B: ";

        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
            fieldB[i+1] = fields[i].getName();
        }

        //Fields and success checkers
        questionType.add("combo");
        questionList.add(fieldA); 
        questionType.add("combo");
        questionList.add(fieldB); 
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("label");
        questionList.add(new String[]{"Success parameters"});
        questionType.add("comparison");
        questionList.add(new String[]{"FieldA(x)    ","<",">","=","    success"}); //\u2260 is uncode for !=         
        questionType.add("text");
        questionList.add(new String[]{"success = "});
        questionType.add("comparison");
        questionList.add(new String[]{"FieldB(x)    ","<",">","=","    success"}); //\u2260 is uncode for !=         
        questionType.add("text");
        questionList.add(new String[]{"success = "});

        
        //Hypothesis
        questionType.add("label");
        questionList.add(new String[]{"\n"}); 
        questionType.add("label");
        questionList.add(new String[]{"Hypothesis"});
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        questionType.add("comparison");
        questionList.add(new String[]{"\u0070\u0302    ","<",">","=","    P\u2080"}); //phat <>= PNull //\u2260 is uncode for !=        
        
        questionType.add("text");
        questionList.add(new String[]{"P\u2080 = "}); // Pnull
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "}); // alpha
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);

//+3
        System.out.println(Arrays.toString(selected)); //TODO: string.equals()? 
        if(selected[0] == "" || selected[1] == ""|| selected[4] == "" || selected[5] == "" || selected[6] == "" || selected[7] == ""|| selected[11] == "" || selected[12] == "") {
            System.out.println("ERROR: Nulls");
            return "";
        }

        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));

        String successADirectionCheck = selected[4];
        double successABound = -1; //selected[5];
        
        String successBDirectionCheck = selected[6];
        double successBBound = -1; //selected[7];
        String nullDirectionCheck = selected[11];
        double pNull = -1; //selected[12]
        double alpha = 0.05;//selected[14]
        
        Direction successADirection;
        switch (successADirectionCheck) {
            case "<":
                successADirection = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                successADirection = Direction.GREATER_THAN;
                break;
            case "=":
                successADirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + successADirectionCheck);
                return "";
        }
        try {
            successABound = Double.parseDouble(selected[5]); //valueOf? 
        } catch (Exception e) {
            System.out.println("ERROR: success bound must be a number.");
            return "";
        }
        
        Direction successBDirection;
        switch (successBDirectionCheck) {
            case "<":
                successBDirection = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                successBDirection = Direction.GREATER_THAN;
                break;
            case "=":
                successBDirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + successBDirectionCheck);
                return "";
        }
        try {
            successABound = Double.parseDouble(selected[7]); //valueOf? 
        } catch (Exception e) {
            System.out.println("ERROR: success bound must be a number.");
            return "";
        }

        try {
            pNull = Double.parseDouble(selected[12]); //valueOf? 
            if(pNull < 0 || pNull > 1) throw new Exception();
        } catch (Exception e) {
            System.out.println("ERROR: P\u2080 must be a number 0-1.");
            return "";
        }
        Direction nullDirection;
        switch (nullDirectionCheck) {
            case "<":
                nullDirection = Direction.LESS_THAN; //TODO: Either test or result backwards; pregnant - glucose rejected
                break;
            case ">":
                nullDirection = Direction.GREATER_THAN;
                break;
            case "=":
                nullDirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + nullDirectionCheck);
                return "";
        }

        if(!selected[14].strip().equals("")) {
            try {
                alpha = Double.parseDouble(selected[14]); //valueOf? 
                if(alpha <= 0 || alpha >= 1) {
                    alpha = 0.05;
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("ERROR: alpha \u03B1 must be a number 0-1 (exclusive). Using default 0.05");
            }
        }
        Proportion proportion = new Proportion(fieldOne, fieldTwo, successABound, successADirection, successBBound, successBDirection, alpha, pNull, nullDirection);
        proportion.setTwoProportion();
        return proportion.printBasic();
    }
}
