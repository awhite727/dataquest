package dataquest;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JScrollPane;

//TODO: Use class to break down each element into a private set and get of the Component
//setComponent should run any necessary checks for that component then return the Component
//getComponent show run necessary submission checks for that component then return the associated String
//TODO: Fix automatic width sizing
//TODO: Add error handling 
//TODO: Add next/previous/submit
    //-- JLabel.setText("<html>Text</html>")/new JLabel("<html>" + text + "</html>")
    //Special characters need to be escaped first, like \<
    //https://stackoverflow.com/questions/2420742/make-a-jlabel-wrap-its-text-by-setting-a-max-width 
public class Popup {
    private JFrame parent;
    private String tabName;
    private ArrayList<QuestionType> questionType;
    //private ArrayList<String> questionType;
    private ArrayList<String[]> questionList;
    private ArrayList<HandleError[]> errors;
    //private ArrayList<String[]> errors;
    private JPanel container;
    private JScrollPane scrollPane;
    private JDialog popup;
    private Dimension minSize;

    Popup(){}
    Popup(JFrame parent, String tabName) {
        this.parent = parent;
        this.tabName = tabName;
        questionType = new ArrayList<>();
        questionList = new ArrayList<>();
        errors = new ArrayList<>();
        designPopup();        
    }
    public enum QuestionType {
        LABEL,
        DROPDOWN,
        BUBBLE,
        TEXT,
        CHECKBOX,
        COMPARISON
    }
    public enum HandleError {
        REQUIRED,
        NUMERIC,
        ZERO_TO_ONE,
        NONE
    }

    //TODO: Delete; used for testing 
    public void printQuestions() {
        if(questionList.size() != questionType.size() || questionType.stream().filter(a -> a!=QuestionType.LABEL).count() != errors.size()) {
            System.out.println("ERROR: sizes are not equal");
            System.out.println("\tqType.size() = " + questionType.size());
            System.out.println("\tqList.size() = " + questionList.size());
            System.out.println("\terrors.size() = " + errors.size());
            return;
        }

        int errorIndex = 0;
        for (int i = 0; i < questionType.size(); i++) {
            System.out.println("Questions " + (i+1) + ": ");
            System.out.println("\tType = " + questionType.get(i));
            System.out.println("\tContents = " + questionList.get(i));
            if(!questionType.get(i).equals(QuestionType.LABEL)) {
                System.out.println("\terrors = " + errors.get(errorIndex));
                errorIndex++;
            }
        }
    }
    //The look of the popup 
    private void designPopup() {
        popup = new JDialog(parent, tabName, true);
        minSize = new Dimension(350,250); //used at bottom
        

        //popup.setLayout(new FlowLayout(FlowLayout.LEFT));
        //Note: Could use a GroupLayout to get the vertical and horizontal aligns right??
            //https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html
        //GroupLayout popupLayout = new GroupLayout(popup);
        //popup.setLayout(popupLayout);
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        //container.setLayout(new FlowLayout(FlowLayout.LEFT));
        scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        popup.add(scrollPane);

        popup.setLocationRelativeTo(parent);
        Dimension containerSize = container.getPreferredSize();
        if(containerSize.getWidth() < minSize.getWidth())
            containerSize.setSize(minSize.getWidth(),containerSize.getHeight());
        if(containerSize.getHeight() < minSize.getHeight())
            containerSize.setSize(containerSize.getWidth(),minSize.getHeight());
        
        popup.setMinimumSize(containerSize);
        popup.setVisible(false);
    }
    

    //public void addQuestion(String qType, String[] contents, String[] error) {
    public void addQuestion(QuestionType qType, String[] contents, HandleError[] error) {
        try {
            switch(qType) {
                case LABEL:
                case TEXT:
                    if(contents.length < 1) {
                        throw new Exception("ERROR: " + qType.toString() + " must have a length of 1");
                    }
                    break;
                case COMPARISON:
                    if(contents.length < 3) {
                        throw new Exception("ERROR: " + qType.toString() + " must have a length of 3");
                    }
                    break;
                default:
                    if(contents.length < 2) {
                        throw new Exception("ERROR: " + qType.toString() + " must have a length of 2");
                    }
                break;
            }
            questionType.add(qType);
            questionList.add(contents);
            errors.add(error);
        } catch (ArrayIndexOutOfBoundsException outError) { //don't think this is called
            System.out.println("ERROR: Index out of bounds; Check the values being passed to addQuestion");
            outError.printStackTrace();
        } catch (Exception e) { //For generic expected errors
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void addQuestions(ArrayList<String> qTypes, ArrayList<String[]> contents, ArrayList<HandleError[]> errors) {
        if (errors == null) errors = new ArrayList<>();
        int sizeMinusLabels = (int) qTypes.stream().filter(a -> !a.equals("label")).count();
        try {
            if(qTypes.size() != contents.size()) {
                throw new Exception("ERROR: addQuestions must have equally sized qType and content ArrayLists");
            }
            //if there are errors present and the size is not equal to the amount of non-label questions, print a warning
            if(errors.size() != 0 && sizeMinusLabels != errors.size()) {
                System.out.println("WARNING: number of errors differs from the number of questions (minus labels). Any errors exceeding the questions will be ignored and any less than the number of questions will automatically be set to none");
            }


            for (String qType : qTypes) {
                switch (qType) {
                    case "label":
                        questionType.add(QuestionType.LABEL);
                        break;
                    case "combo":
                        questionType.add(QuestionType.DROPDOWN);
                        break;
                    case "radio":
                        questionType.add(QuestionType.BUBBLE);
                        break;
                    case "text":
                        questionType.add(QuestionType.TEXT);
                        break;
                    case "check":
                        questionType.add(QuestionType.CHECKBOX);
                        break;
                    case "comparison":
                        questionType.add(QuestionType.COMPARISON);
                        break;
                    default:
                        throw new Exception("ERROR: qType not a valid type: " + qType);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        questionList.addAll(contents);
        this.errors.addAll(errors);
        /* for (HandleError[] error : errors) {
            if (error == null) { //Way to make the error none
                this.errors.add(n)
                continue; 
            }
            
            try {
                switch (error) {
                    case "required":
                        this.errors.add(HandleError.REQUIRED);
                        break;
                    case "numeric":
                        this.errors.add(HandleError.NUMERIC);
                        break;
                    case "":
                    case "none":
                    case "na":
                        this.errors.add(HandleError.NONE);
                        break;
                    default:
                    throw new Exception("ERROR: error not a valid type: " + error + "\n\tSetting type to none");
                }
            } catch (Exception e) {
                System.out.println(e);
                this.errors.add(HandleError.NONE);
            }
        } */
        if(errors.size() < sizeMinusLabels) {
            for (int i = errors.size(); i < sizeMinusLabels; i++) {
                this.errors.add(null);
            }
        }
    }


    //returns null if it can't be constructed; if it can be, returns the values in order 
    public String[] showGenericPopup() {
        System.out.println("showGeneric called");
        if(parent == null || tabName == null) return null;
        ArrayList<Object> mainComponents = new ArrayList<>();
        ArrayList<Component> allComponents = new ArrayList<>();
        System.out.println("Types: " + questionType);

        for (int i = 0; i < questionType.size(); i++) {
            try {
                //If it's a label/textbox it shouldn't have any other elements in the String[] so continue
                //If it's a combo/radio and it only had the title, print error and continue
                QuestionType qType = questionType.get(i);
                String[] question = questionList.get(i);
                switch(qType) {
                    case LABEL: 
                        JLabel tempLabel = new JLabel(question[0]);
                        //mainComponents.add(null);
                        allComponents.add(tempLabel);
                        break; //continue;
                    case TEXT:
                        JPanel textPanel = new JPanel(new GridLayout(2,1)); 
                        textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        textPanel.add(new JLabel(question[0]));
                        TextField field = new TextField();                        
                        field.setSize(new Dimension(50, 30));
                        mainComponents.add(field);
                        textPanel.add(field);
                        allComponents.add(textPanel);
                        break;
                    case DROPDOWN: case COMPARISON:
                        JPanel comboPanel = new JPanel();
                        comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        comboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        comboPanel.add(new JLabel(question[0]));
                        question[0] = "";
                        String compareTemp = "";
                        if(qType.equals(QuestionType.COMPARISON)) {
                            compareTemp = question[question.length-1];
                            question = Arrays.copyOfRange(question, 0, question.length-1);
                        }
                        JComboBox<String> comboBox = new JComboBox<>(question);
                        comboBox.setMinimumSize(new Dimension(100,comboBox.getPreferredSize().height));
                        comboPanel.add(comboBox);
                        mainComponents.add(comboBox);
                        if(qType.equals(QuestionType.COMPARISON)) comboPanel.add(new JLabel(compareTemp));
                        allComponents.add(comboPanel);
                        break;
                    case BUBBLE:
                        System.out.println("Bubble called");
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
                        break;
                    case CHECKBOX:
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
                        break;
                    default:
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
        String[] selectedValues = new String[mainComponents.size()];
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            String[] tempSelected = submission(mainComponents);
            if(tempSelected != null) { //if there was no error
                for (int i = 0; i < selectedValues.length; i++) {
                    selectedValues[i] = tempSelected[i];
                }
                popup.dispose();
            }
            
        });

        allComponents.add(confirmButton);
        for (Component c : allComponents) {
            container.add(c);
        }
        popup.setVisible(true);
        return selectedValues;
    }

    private String[] submission(ArrayList<Object> mainComponents){
        String[] selectedValues = new String[mainComponents.size()];
        try {
            int typeIndex = 0;
            int componentIndex = 0;
            while(typeIndex < questionType.size()) {
                QuestionType qType = questionType.get(typeIndex);
                if(qType.equals(QuestionType.LABEL)) { //not added to main component, don't need to examine
                    typeIndex++;
                    continue;
                }
                Object box = mainComponents.get(componentIndex);
                //System.out.println("Main component: " + box);
                switch(qType) {
                    case LABEL: break;
                    case DROPDOWN: case COMPARISON:
                        try {
                            selectedValues[componentIndex] = (String) ((JComboBox<String>) box).getSelectedItem();
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + componentIndex + ") to a JComboBox<String>");
                            System.out.println("\t"+mainComponents.get(componentIndex));
                        } 
                        break;
                    case BUBBLE: 
                        try {
                            JRadioButton[] radioButtons = (JRadioButton[]) box;
                            for (JRadioButton radioButton : radioButtons) {
                                if (radioButton.isSelected()) {
                                    selectedValues[componentIndex] = radioButton.getText();
                                    break;
                                }
                            }
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + componentIndex + ") to a JRadioButton[]");
                            System.out.println("\t" + mainComponents.get(componentIndex));
                        }
                        break;
                    case CHECKBOX:
                        try {
                            JCheckBox[] checkButtons = (JCheckBox[]) box;
                            ArrayList<String> checkString = new ArrayList<>();
                            for (JCheckBox checkButton : checkButtons) {
                                if (checkButton.isSelected()) {
                                    checkString.add(checkButton.getText());
                                }
                                selectedValues[componentIndex] = String.join(",", checkString);
                            }
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + componentIndex + ") to a JCheckBox[]");
                            System.out.println("\t" + mainComponents.get(componentIndex));
                        }
                        break;
                    case TEXT:
                        try {
                            selectedValues[componentIndex] = ((TextField)box).getText();
                        } catch (Exception e1) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + componentIndex + ") to a TextField");
                            System.out.println("\t" + mainComponents.get(componentIndex));
                        }
                    break;
                    default: throw new Exception("ERROR: Unknown type " + qType);
                }
                //Check for errors, return null if there is one 
                if(errors.size() > componentIndex)
                    if(!errorHandling(errors.get(componentIndex),selectedValues[componentIndex])) return null;
                typeIndex++;
                componentIndex++;
            }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        return selectedValues;
    }

    private boolean errorHandling(HandleError[] handleErrors, String value){
        boolean success = true;
        if(handleErrors == null || (handleErrors.length == 1 && handleErrors[0] == HandleError.NONE)) return true;
        for (HandleError error : handleErrors) {
            switch (error) {
            case NONE: //Shouldn't be reached if passed properly, but if it isn't it's ignored
                break;
            case REQUIRED: 
                if(value == null || value.equals("")) {
                    success = false;
                    System.out.println("Required field not present (" + value + ")");
                }
                break;
            case NUMERIC:
                if(value == null || value.equals("")) continue; //handles required separately
                try {
                    Double.valueOf(value);
                } catch (Exception e) {
                    success = false;
                    System.out.println("Numeric field cannot be turned into double (" + value + ")");
                }
                break;
            case ZERO_TO_ONE:
            if(value == null || value.equals("")) continue; //handles required separately
                try {
                    double val = Double.valueOf(value);
                    if(val <= 0 || val >= 1) {
                        System.out.println("Value not between 0-1 (exclusive): " + val);
                        success = false;
                    }
                } catch (Exception e) {
                    success = false;
                    System.out.println("Zero-to-one field cannot be turned into double (" + value + ")");
                }
                break;
            default:
                System.out.println("ERROR: Unhandled new HandleError: " + error);
                success = false;
                break;
            }
        }
        System.out.println("Success: " + success);
        return success;
    }












    // displays a popup with specified choices and returns the results
    // Allowed questionTypes: 
        /* "label" - no specified question; just for display text
         * "combo" - dropdown box 
         * "radio" - bubbles -- returns null if nothing selected
         * "text" - textbox -- returns empty string if nothing added
         * "check" - checkbox (radio but allowing multiple to be selected) -- returns empty string if nothing added
         * "comparison" - special combo that allows a label before and after the combo; format as label, all combo items, label
         */
    // questionList format:  
    /* index 0 - question name; if none to be used use empty string
     * index 1-n: any options; if a label or text does not need to be included
     */
    //error format: (currently assumes String[] of exactly length 3)
    /* index 0 - String representation of the index of what should be checked (index within questionType)
     * index 1 - type of check (i.e. "required", "exactMatch")
     * index 2 - (optional) value to check (i.e. "Confirm")
     */
    // returns String[] of the results in the index; labels are returned as null
    //NOTE: Currently all questions optional by default
    public static String[] showGenericPopup(JFrame parent, String tabName, ArrayList<String> questionType, ArrayList<String[]> questionList, ArrayList<String[]> errors) { 
        ArrayList<Object> mainComponents = new ArrayList<>();
        ArrayList<Component> allComponents = new ArrayList<>();
        JDialog popup = new JDialog(parent, tabName, true);
        //popup.setSize(350, 250);
        Dimension minSize = new Dimension(350,250);
        //popup.setMinimumSize(minSize);
        

        //popup.setLayout(new FlowLayout(FlowLayout.LEFT));
        //Note: Could use a GroupLayout to get the vertical and horizontal aligns right??
            //https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html
        //GroupLayout popupLayout = new GroupLayout(popup);
        //popup.setLayout(popupLayout);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        //container.setLayout(new FlowLayout(FlowLayout.LEFT));
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        popup.add(scrollPane);
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
                    JPanel textPanel = new JPanel(new GridLayout(2,1)); 
                    //JPanel textPanel = new JPanel( new GridBagLayout() );
                    //textPanel.setBackground(Color.RED);
                    textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    textPanel.add(new JLabel(question[0]));
                    //textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS)); 
                    TextField field = new TextField();
                    
                    
                    //field.setPreferredSize();
                    field.setSize(new Dimension(50, 30));
                    mainComponents.add(field);
                    textPanel.add(field);
                    /* textPanel.add( field, new GridBagConstraints( 0, 0, 1, 1, 1.0,
                        1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets( 0, 0, 0, 0 ), 0, 0 ) ); */
                    allComponents.add(textPanel);
                }
                else if((question.length < 2)||(qType.equals("comparison") && question.length < 3)){ 
                    mainComponents.add(null);
                    throw new Exception ("ERROR: questionList.get("+i+") has length of " + question.length + " and is labelled as a " + qType);
                }
                else if(qType.equals("combo") || qType.equals("comparison")) {
                    JPanel comboPanel = new JPanel();
                    comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    comboPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    //System.out.println(question[0]);
                    comboPanel.add(new JLabel(question[0]));
                    question[0] = "";
                    String compareTemp = "";
                    if(qType.equals("comparison")) {
                        compareTemp = question[question.length-1];
                        question = Arrays.copyOfRange(question, 0, question.length-1);
                    }
                    //JComboBox<String> comboBox = new JComboBox<>(Arrays.copyOfRange(question, 1, question.length));
                    JComboBox<String> comboBox = new JComboBox<>(question);
                    //comboBox.setPreferredSize(new Dimension(200,comboBox.getPreferredSize().height));
                    comboBox.setMinimumSize(new Dimension(100,comboBox.getPreferredSize().height));
                    comboPanel.add(comboBox);
                    mainComponents.add(comboBox);

                    if(qType.equals("comparison")) {
                        comboPanel.add(new JLabel(compareTemp));
                    }

                    //comboPanel.setBackground(Color.RED); //TODO: Allow text to grow when resizing window?
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
            String[] tempSelected = submission(questionType,mainComponents,errors);
            for (int i = 0; i < selectedValues.length; i++) {
                selectedValues[i] = tempSelected[i];
            }
            popup.dispose();
        });

        allComponents.add(confirmButton);

        //System.out.println("Total components: " + allComponents.size());
        for (Component c : allComponents) {
            container.add(c);
            //System.out.println("Added: "  + c + "\n");
        }
        popup.setLocationRelativeTo(parent);
        Dimension containerSize = container.getPreferredSize();
        if(containerSize.getWidth() < minSize.getWidth())
            containerSize.setSize(minSize.getWidth(),containerSize.getHeight());
        if(containerSize.getHeight() < minSize.getHeight())
            containerSize.setSize(containerSize.getWidth(),minSize.getHeight());
        
        popup.setMinimumSize(containerSize);
        popup.setVisible(true);

        //System.out.println("\n\n\n\n\n\n~~~~~~\n\n\n\n\n");
        /*Component[] component = ((JPanel)((JViewport)((JScrollPane)((JPanel)((JLayeredPane)((JRootPane)popup.getComponent(0)).getComponent(1)).getComponent(0)).getComponent(0)).getComponent(0)).getComponent(0)).getComponents();
        for (Component c : component) {
            System.out.println("Popup: " + c.getClass().getName());
            if((c instanceof JPanel) && (((JPanel)c).getComponentCount() > 2)){
                System.out.println("\tComparison: " );
                for (Component c2 : ((JPanel)c).getComponents()) {
                    if(c2 instanceof JLabel){
                        System.out.println("\t->"+((JLabel)c2).getText());
                    }
                }
            }
        } */


        return selectedValues;
    }

    private static String[] submission(ArrayList<String> questionType, ArrayList<Object> mainComponents, ArrayList<String[]> errors){
        String[] selectedValues = new String[questionType.size()];
        try {
            for (int i = 0; i < questionType.size(); i++) {
                String qType = questionType.get(i); 
                Object box = mainComponents.get(i); //WILL HAVE ERROR? -- 3/26/25 I have no idea what I was meaning about errors here. It seems to be working fine.
                if (qType.equals("label")) {
                    continue;
                } else if (qType.equals("combo") || qType.equals("comparison")) {
                    try {
                        selectedValues[i] = (String) ((JComboBox<String>) box).getSelectedItem();
                    } catch (ClassCastException classCastError) {
                        System.out.println("ERROR: Could not cast mainComponents.get(" + i + ") to a JComboBox<String>");
                    }
                } /* else if (qType.equals("comparison")) {
                    try {
                        selectedValues[i] = (String) ((JComboBox<String>) box).getSelectedItem();
                    } catch (ClassCastException classCastError) {
                        System.out.println("ERROR: Could not cast mainComponents.get(" + i + ") to a JComboBox<String>");
                    }
                }  */else if(qType.equals("radio")) {
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
        return selectedValues;
    }
}
