
package dataquest;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;

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
         * "radio" - bubbles
         */
    // for each element in questionList index 0 assumed to be the question name; if none to be used use ""
    // returns String[] of the results in the index; labels are returned as null
    //NOTE: Currently all questions automatically optional
    //If not included it will just return null
    private static String[] showGenericPopup(JFrame parent, String tabName, ArrayList<String> questionType, ArrayList<String[]> questionList) {
        ArrayList<Object> components = new ArrayList<>();
        JDialog popup = new JDialog(parent, tabName, true);
        popup.setSize(350, 250);
        popup.setLayout(new FlowLayout());
        
        System.out.println("Types: " + questionType);

        for (int i = 0; i < questionType.size(); i++) {
            String qType = questionType.get(i);
            String[] question = questionList.get(i);
            try {
                if(question.length < 1) {
                    components.add(null);
                    throw new Exception("ERROR: questionList.get("+i+") has a length of " + question.length);
                }
                //Print label
                /* System.out.println("Adding label: " + question[0]);
                components.add(new JLabel(question[0]));
                System.out.println("\t" + ((JLabel) components.get(components.size()-1)).getText());
                popup.add((Component)components.get(components.size()-1)); */

                //If it's a label it shouldn't have any other elements in the String[] so continue
                //If it's not a label and it only had the title, print error and continue
                //If it's not a label and it has at least 1 option, run checks of what type of question it is
                if (qType.equals("label")){
                    components.add(null);
                    popup.add(new JLabel(question[0]));
                    continue;
                }
                else if(question.length < 2){ 
                    components.add(null);
                    throw new Exception ("ERROR: questionList.get("+i+") has length of " + question.length + " and is labelled as a " + qType);
                }
                else if(qType.equals("combo")) {
                    popup.add(new JLabel(question[0]));
                    components.add(new JComboBox<>(Arrays.copyOfRange(question, 1, question.length)));
                    popup.add((Component)components.get(components.size()-1));
                } else if(qType.equals("radio")) {
                    String[] radioOptions = Arrays.copyOfRange(question, 1, question.length);
                    ButtonGroup radioGroup = new ButtonGroup();
                    JPanel radioPanel = new JPanel();
                    radioPanel.add(new JLabel(question[0]));
                    JRadioButton[] radioButtons = new JRadioButton[radioOptions.length];
                    radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS)); // vertical stacking
                    for (int j = 0; j < radioOptions.length; j++) {
                        radioButtons[j] = new JRadioButton(radioOptions[j]);
                        radioGroup.add(radioButtons[j]);
                        radioButtons[j].setAlignmentX(Component.RIGHT_ALIGNMENT); // align to the right so buttons are in a line
                        radioPanel.add(radioButtons[j]);
                    }
                    components.add(radioButtons);
                    popup.add(radioPanel);
                    
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
                    Object box = components.get(i); //WILL HAVE ERROR
                    if (qType.equals("label")) {
                        continue;
                    } else if (qType.equals("combo")) {
                        try {
                            selectedValues[i] = (String) ((JComboBox<String>) box).getSelectedItem();
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast components.get(" + i + ") to a JComboBox<String>");
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
                            System.out.println("ERROR: Could not cast components.get(" + i + ") to a JRadioButton[]");
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException collectError) {
                System.out.println("ERROR: showGenericPopup");
                collectError.printStackTrace();
            }
            popup.dispose();
        });

        popup.add(confirmButton);
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
        return selectedValues;
    }


    public static String histogramMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        String binSize = "Bin Size"; //just to prevent naming errors
        String numBins = "Number of Bins"; //just to prevent naming errors
        if (fields.length==0) {
            return "No numerical fields available for histogram.";
        }
        String [] fieldNames = new String[fields.length+1];
        fieldNames[0] = "Field";
        for (int i=0;i<fields.length;i++) {
            fieldNames[i+1] = fields[i].getName();
        }
        String[] options = new String[]{"Shape Using", binSize, numBins};
        
        String[] selected = showComboAndRadioPopup(parent, "Histogram", "Field", "Shape Using", fieldNames, options);
        //String[] selected = showGenericPopup(parent, "Histogram", new ArrayList<String>(Arrays.asList("combo","radio")),new ArrayList<String[]>(Arrays.asList(fieldNames,options)));
        System.out.println("Selected size: " + selected.length);
        for (String s : selected) {
            System.out.println("\t" + s);
        }
        if(selected[0] == null || selected[1]==null) return null;
        Field summaryField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Histogram histogram = new Histogram(summaryField);
        String textOutput = "";
        if(selected[1].equals(binSize)) {
            textOutput = histogram.binFromSize(1);
        }
        else if(selected[1].equals(numBins)) {
            textOutput = histogram.binFromNum(10);
        } else{ //shouldn't be called but just in case
            textOutput = "ERROR: Shape determination not defined. Using default\n";
            textOutput += histogram.binFromNum(10);
        } 
        return textOutput;
        //return "";
   }
}
