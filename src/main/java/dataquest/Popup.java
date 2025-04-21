package dataquest;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

//TODO: Fix automatic width sizing
    //-- JLabel.setText("<html>Text</html>")/new JLabel("<html>" + text + "</html>")
    //Special characters need to be escaped first, like \<
    //https://stackoverflow.com/questions/2420742/make-a-jlabel-wrap-its-text-by-setting-a-max-width 
public class Popup {
    private JFrame parent;
    private String tabName;

    private ArrayList<QuestionType> questionType;
    private ArrayList<String[]> questionList;
    private ArrayList<HandleError[]> errors;
    private ArrayList<Object[]> questionNames;
    private ArrayList<Object[]> optionalConditionals;
    private ArrayList<Object[]> conditionalErrors; //Each Object[] contains an int followed by n Object[]'s, each with the value followed by all needed HandleErrors
    private ArrayList<Integer> minFieldNs;
    private ArrayList<String> customErrorMessages; //Just puts it on a new line, still includes the previous message
    private String[] selected;
    
    private JPanel container;
    private JScrollPane scrollPane;
    private JDialog popup;
    private Dimension minSize;

    private static ArrayList<Object> mainComponents;
    private static ArrayList<Component> allComponents;

    Popup(JFrame parent, String tabName) {
        this.parent = parent;
        this.tabName = tabName;
        questionType = new ArrayList<>();
        questionList = new ArrayList<>();
        errors = new ArrayList<>();
        optionalConditionals = new ArrayList<>();
        conditionalErrors = new ArrayList<>();
        questionNames = new ArrayList<>();
        minFieldNs = new ArrayList<>();
        customErrorMessages = new ArrayList<>();
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
        INTEGER,
        POSITIVE_VALUE,
        ZERO_TO_ONE,
        OPTIONAL_CONDITIONAL, //For requiring a question only if another question has a certain value 
        MINIMUM_FIELD_N, //For requiring a selected field have an n of at least __
        CONDITIONAL_ERROR, //For if a previous selection causes new error checks for a current question
        CUSTOM_ERROR_MESSAGE, //Doesn't actually check anything; if success is false then a check is made if there's a custom error message to use 
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
            System.out.println("Question " + (i+1) + ": ");
            System.out.println("\tType = " + questionType.get(i));
            System.out.println("\tContents = " + Arrays.toString(questionList.get(i)));
            if(!questionType.get(i).equals(QuestionType.LABEL)) {
                System.out.println("\terrors = " + Arrays.toString(errors.get(errorIndex)));
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
            
            if(!qType.equals(QuestionType.LABEL)) {
                errors.add(error);
                int qListIndex = questionList.lastIndexOf(contents);
                questionNames.add(new Object[]{qListIndex, questionList.get(qListIndex)[0].strip()  });
            }
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

            int errorIndex = 0;
            for (int i=0; i < qTypes.size(); i++) {
                String qType = qTypes.get(i);
                if(!qType.equals("label")) {
                    questionNames.add(new Object[]{errorIndex,contents.get(i)[0].strip()});
                    this.errors.add(errors.get(errorIndex));
                    errorIndex++;
                }
                questionList.add(contents.get(i));
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
        if(errors.size() < sizeMinusLabels) {
            for (int i = errors.size(); i < sizeMinusLabels; i++) {
                this.errors.add(null);
            }
        }
    }

    //pass the index of the prior mainComponent/error that needs to meet a condition 
    //and the String it must match for the current question to be required
    public void addOptionalConditional(int compIndex, String value) {
        optionalConditionals.add(new Object[]{compIndex,value});
    }

    //pass the index of the prior mainComponent/error that needs to meet a condition
    //and an ArrayList<Object[]> with 
        //Object[0] = value it can equal
        //Object[1-n] = HandleErrors 
        //NOTE: complex functions (i.e. MINIMUM_FIELD_N) not handled
    public void addConditionalErrors(int compIndex, ArrayList<Object[]> conditionals) {
        Object[] conditionalBuilder = new Object[conditionals.size()+1];
        conditionalBuilder[0] = compIndex;
        for (int i = 0; i < conditionals.size(); i++) {
            conditionalBuilder[i+1] = conditionals.get(i);
        }
        conditionalErrors.add(conditionalBuilder);
    }

    //pass the minimum count needed
    //should be called in question order
    public void addMinFieldN(int minCount) {
        minFieldNs.add(minCount);
    }

    public void addCustomMessage(String message) {
        customErrorMessages.add(message);
    }
    //For error messages, has default of the attached label
    public void setQuestionName(int errorIndex, String newValue){
        Object[] question = questionNames.get(errorIndex);
        question[1] = newValue;
        questionNames.set(errorIndex, question);
    }


    //returns null if it can't be constructed; if it can be, returns the values in order 
    public String[] showGenericPopup() {
        System.out.println("showGeneric called");
        if(parent == null || tabName == null) return null;
        mainComponents = new ArrayList<>();
        allComponents = new ArrayList<>();
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
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            selected = submission();
            if(selected != null) popup.dispose();
        });

        //Ensures that it returns null if Xed out to make checks easier   
        popup.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e)
                {
                    selected = null; 
                    e.getWindow().dispose();
                }
            }
        );

        allComponents.add(confirmButton);
        for (Component c : allComponents) {
            container.add(c);
        }
        popup.setVisible(true);
        return selected;
    }

    private String[] submission(){
        selected = new String[mainComponents.size()];
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
                            selected[componentIndex] = (String) ((JComboBox<String>) box).getSelectedItem();
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
                                    selected[componentIndex] = radioButton.getText();
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
                                selected[componentIndex] = String.join(",", checkString);
                            }
                        } catch (ClassCastException classCastError) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + componentIndex + ") to a JCheckBox[]");
                            System.out.println("\t" + mainComponents.get(componentIndex));
                        }
                        break;
                    case TEXT:
                        try {
                            selected[componentIndex] = ((TextField)box).getText();
                        } catch (Exception e1) {
                            System.out.println("ERROR: Could not cast mainComponents.get(" + componentIndex + ") to a TextField");
                            System.out.println("\t" + mainComponents.get(componentIndex));
                        }
                    break;
                    default: throw new Exception("ERROR: Unknown type " + qType);
                }
                //Check for errors, return null if there is one 
                HandleError[] handleErrors = errors.get(componentIndex);
                System.out.println("************Question: " + Arrays.toString(questionList.get(typeIndex)));
                if(!(handleErrors == null || (handleErrors.length == 1 && handleErrors[0] == HandleError.NONE))) {
                    for (HandleError handleError : handleErrors) {
                        if(!errorHandling(typeIndex,componentIndex, handleError, selected[componentIndex],(String)(questionNames.get(componentIndex)[1]))) return null;
                    }
                }
                /* 
                if(errors.size() > componentIndex) {
                    if(!errorHandling(typeIndex,componentIndex,handleErrors,selected[componentIndex],(String)(questionNames.get(componentIndex)[1]))) return null;    
                } */
                typeIndex++;
                componentIndex++;
            }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        return selected;
    }

    private String uniqueErrorHandling(int typeIndex, int componentIndex) {
        QuestionType qType = questionType.get(typeIndex);
        final int tempComp = componentIndex; //avoid need to be final
        //if optional_conditional, send to seprate method
        if (Arrays.stream(errors.get(componentIndex)).anyMatch(a-> a==HandleError.OPTIONAL_CONDITIONAL)) {
            System.out.println("Optional Conditional");
            int optionalIndex = (int) errors.stream().
            filter(prior -> (errors.indexOf(prior) < tempComp)). //stop stream at the current index
                filter(a ->
                    Arrays.stream(a).anyMatch(b -> b.equals(HandleError.OPTIONAL_CONDITIONAL)) //if any of the errors contain, count it ONCE
            ).count();
            int neededPriorIndex = (int) optionalConditionals.get(optionalIndex)[0];
            //obtain the previous question
            Integer[] nonLabelIndexes = questionType.stream()
            .map(a -> (!a.equals(QuestionType.LABEL)) ? questionType.indexOf(a) : -1)
            .filter(a-> a!=-1).collect(Collectors.toList()).toArray(new Integer[0]);//.filter(a -> !a.equals(QuestionType.LABEL));
            int priorQuestionIndex = nonLabelIndexes[neededPriorIndex];
            System.out.println("************Question (optionalConditional): " + questionList.get(typeIndex)[0]);
            //if(isValidOptional(optionalIndex,(String)(questionNames.get(priorQuestionIndex)[1]),selected[neededPriorIndex],(String)(questionNames.get(componentIndex)[1]),selected[componentIndex])) return false;
            return isValidOptional(optionalIndex,(String)(questionNames.get(priorQuestionIndex)[1]),selected[neededPriorIndex],(String)(questionNames.get(componentIndex)[1]),selected[componentIndex]);
        } 

        //handle minimum_Field_N separately 
        else if (Arrays.stream(errors.get(componentIndex)).anyMatch(a-> a==HandleError.MINIMUM_FIELD_N)) {
            System.out.println("Minimum field n");
            int minmumIndex = (int) errors.stream().
            filter(prior -> (errors.indexOf(prior) < tempComp)). //stop stream at the current index
                filter(a ->
                    Arrays.stream(a).anyMatch(b -> b.equals(HandleError.MINIMUM_FIELD_N)) //if any of the errors contain, count it ONCE
            ).count();
            final boolean check = qType.equals(QuestionType.CHECKBOX);
            System.out.println("************Question (minIndex): " + questionList.get(typeIndex)[0]);
            //if(!isValidMin(minmumIndex,(String)(questionNames.get(componentIndex)[1]), selected[componentIndex],check)) return false;
            return isValidMin(minmumIndex,(String)(questionNames.get(componentIndex)[1]), selected[componentIndex],check);
        }

        //handle conditional errors separately 
        else if (Arrays.stream(errors.get(componentIndex)).anyMatch(a-> a==HandleError.CONDITIONAL_ERROR)) {
            System.out.println("Conditional error");
            int conditionalIndex = (int) errors.stream().
            filter(prior -> (errors.indexOf(prior) < tempComp)). //stop stream at the current index
                filter(a ->
                    Arrays.stream(a).anyMatch(b -> b.equals(HandleError.CONDITIONAL_ERROR)) //if any of the errors contain, count it ONCE
            ).count();
            int neededPriorIndex = (int) conditionalErrors.get(conditionalIndex)[0];
            //obtain the previous question
            Integer[] nonLabelIndexes = questionType.stream().map(a -> (!a.equals(QuestionType.LABEL)) ? questionType.indexOf(a) : -1).filter(a-> a!=-1).collect(Collectors.toList()).toArray(new Integer[0]);//.filter(a -> !a.equals(QuestionType.LABEL));
            int priorQuestionIndex = nonLabelIndexes[neededPriorIndex];
            System.out.println("************Question (validConditional): " + questionList.get(typeIndex)[0]);
            //if(!isValidConditional(conditionalIndex, (String)(questionNames.get(priorQuestionIndex)[1]), selected[neededPriorIndex], (String)(questionNames.get(componentIndex)[1]), selected[componentIndex])) return false;
            return isValidConditional(conditionalIndex, (String)(questionNames.get(priorQuestionIndex)[1]), selected[neededPriorIndex], (String)(questionNames.get(componentIndex)[1]), selected[componentIndex]);
        }
        else {//Unhandled Error type 
            System.out.println("ERROR with type: " + errors.get(componentIndex));
            return "ERROR: System error";
        }
    }
    //String question only used for error messages
    private boolean errorHandling(int typeIndex, int errorIndex, HandleError error, String value, String question){
        boolean success = true;
        String errorMessage = "";
        ///HandleError[] handleErrors = errors.get(errorIndex);
        //if(handleErrors == null || (handleErrors.length == 1 && handleErrors[0] == HandleError.NONE)) return true;
            switch (error) {
            case NONE: //Shouldn't be reached if passed properly, but if it isn't it's ignored
                break;
            case CUSTOM_ERROR_MESSAGE: break; //Not an actual error, just a way to signal a unique way to handle one
            case REQUIRED: 
                if(value == null || value.equals("")) {
                    success = false;
                    errorMessage = "Question \"" + question + "\" required";
                }
                break;
            case NUMERIC:
                if(value == null || value.equals("")) return true; //handles required separately
                try {
                    Double.valueOf(value);
                } catch (Exception e) {
                    success = false;
                    errorMessage = "Question \"" + question + "\" must be numeric";
                }
                break;
            case INTEGER:
                if(value == null || value.equals("")) return true; //handles required separately
                try {
                    Integer.valueOf(value);
                } catch (Exception e) {
                    success = false;
                    errorMessage = "Question \"" + question + "\" must be an integer";
                }
                break;
            case ZERO_TO_ONE:
                if(value == null || value.equals("")) return true; //handles required separately
                    try {
                        double val = Double.valueOf(value);
                        if(val <= 0 || val >= 1) {
                            errorMessage = "Question \"" + question + "\" must be a number 0-1 (exclusive)";
                            success = false;
                        }
                    } catch (Exception e) {
                        success = false;
                        errorMessage = "Question \"" + question + "\" must be a number 0-1 (exclusive)";
                    }
                break;
            case POSITIVE_VALUE:
                if(value == null || value.equals("")) return true; //handles required separately
                try {
                    double val = Double.valueOf(value);
                    if(val < 0) {
                        errorMessage = "Question \"" + question + "\" must be positive";
                        success = false;
                    }
                } catch (Exception e) {
                    success = false;
                    errorMessage = "Question \"" + question + "\" must be a positive number";
                }
                break;
            default:
                errorMessage = uniqueErrorHandling(typeIndex, errorIndex);
                if(!errorMessage.equals("")) success = false;
                break;
            }
        
        System.out.println("Success: " + success);
        //If there isn't a success, check if error.get(errorIndex) contains a custom error message   
        if(!success) {
            HandleError[] errors = this.errors.get(errorIndex);
            if(Arrays.stream(errors).anyMatch(a -> a.equals(HandleError.CUSTOM_ERROR_MESSAGE))) {
                int errMsgIndex = (int) this.errors.stream().
                    filter(prior -> (this.errors.indexOf(prior) < errorIndex)). //stop stream at the current index
                        filter(a ->
                            Arrays.stream(a).anyMatch(b -> b.equals(HandleError.CUSTOM_ERROR_MESSAGE)) //if any of the errors contain, count it ONCE
                    ).count();
                errorMessage = errorMessage + "\n" + customErrorMessages.get(errMsgIndex);
            }
            showErrorMessage(errorMessage);
        }
        return success;
    }


    //Within optionalConditionals: 
        //Object[0] = what the prior question has a need
        //Object[1] = what the prior question needs to equal
    //if condition not met, value should be null/""
    //if condition met, value should not be null/""
    //Note: Doesn't handle lists; if needed check "isValidMin" for reference of how to handle
    private String isValidOptional(int optionalIndex, String priorQuestion, String priorObtained, String currentQuestion, String currentObtained){
        boolean success = false;
        String errorMessage = "";
        Object[] conditions = optionalConditionals.get(optionalIndex);
        if(priorObtained.equals(conditions[1])) { //condition met, value should not be null
            System.out.println(priorObtained + " equals " + conditions[1]);
            if(currentObtained == null || currentObtained.equals("")) {
                //showErrorMessage("When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" must filled");
                errorMessage = "\"" + priorObtained + "\" must be specified.";
                success = false;
            }
            else success = true;
        } else { //else value should be null 
            System.out.println(priorObtained + " does not equal " + conditions[1]);
            if(currentObtained == null || currentObtained.equals("")) success = true;
            else {
                success = false;
                //showErrorMessage("When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" must not filled");
                errorMessage = "Remove value from \"" + currentQuestion + "\"";
                
            }
        }
        System.out.println("Success: " + success);
        
        return errorMessage;
    }

    private String isValidConditional(int conditionalIndex, String priorQuestion, String priorObtained, String currentQuestion, String currentObtained) {
        boolean success = true;
        Object[] conditions = conditionalErrors.get(conditionalIndex);
        String errorMessage = "";
        //conditions[0] = prior index
        //conditions[1-n] = Object[] specifics
            //specifics[0] = value to check
            //specifics[1-n] = HandleErrors
        //if(currentObtained == null || priorObtained.equals("")) return true;

        for (int i = 1; i < conditions.length; i++) {
            Object[] specific = ((Object[])conditions[i]);
            if(specific[0].equals(priorObtained)) { //if the condition is met, for loop of basic error checks
                System.out.println("Prior condition met: " + specific[0] + " = " + priorObtained);
                for (int j = 1; j < specific.length; j++) {
                    HandleError error = (HandleError)specific[j];
                    System.out.println("Handling error: " + error.name());
                    switch (error) {
                        case NONE: //Shouldn't be reached if passed properly, but if it isn't it's ignored
                            break;
                        case REQUIRED: 
                            if(currentObtained == null || currentObtained.equals("")) {
                                success = false;
                                errorMessage = ("When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" is required");
                                System.out.println("Required field not present (" + currentObtained + ")");
                            }
                            break;
                        case NUMERIC:
                            if(currentObtained == null || currentObtained.equals("")) continue; //handles required separately
                            try {
                                Double.valueOf(currentObtained);
                            } catch (Exception e) {
                                success = false;
                                System.out.println("Numeric field cannot be turned into double (" + currentObtained + ")");
                                errorMessage = "When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" must be numeric";
                            }
                            break;
                        case INTEGER:
                            if(currentObtained == null || currentObtained.equals("")) continue; //handles required separately
                            try {
                                Integer.valueOf(currentObtained);
                            } catch (Exception e) {
                                success = false;
                                errorMessage = "When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" must be an integer";
                            }
                            break;
                        case ZERO_TO_ONE:
                            if(currentObtained == null || currentObtained.equals("")) continue; //handles required separately
                                try {
                                    double val = Double.valueOf(currentObtained);
                                    if(val <= 0 || val >= 1) {
                                        System.out.println("Value not between 0-1 (exclusive): " + val);
                                        errorMessage = "When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" must be between 0-1 (exclusive) ";
                                        success = false;
                                    }
                                } catch (Exception e) {
                                    success = false;
                                    System.out.println("Zero-to-one field cannot be turned into double (" + currentObtained + ")");
                                    errorMessage = "When \"" + priorQuestion + "\" is \"" + priorObtained + "\", \"" + currentQuestion + "\" must be a value 0-1 (exclusive)";
                                }
                            break;
                        case POSITIVE_VALUE:
                            if(currentObtained == null || currentObtained.equals("")) return ""; //handles required separately
                            try {
                                double val = Double.valueOf(currentObtained);
                                if(val <= 0) {
                                    errorMessage = "When \"" + priorQuestion + "\" is \"" + priorObtained + "\", Question \"" + currentQuestion + "\" must be positive";
                                    success = false;
                                }
                            } catch (Exception e) {
                                success = false;
                                errorMessage = "When \"" + priorQuestion + "\" is \"" + priorObtained + "\", Question \"" + currentQuestion + "\" must be positive";
                            }
                            break;
                            
                        default:
                            System.out.println("ERROR: Unhandled new HandleError for Conditionals: " + error);
                            success = false;
                            break;
                    }
                    if(!success) break; //break if there is any failure 
                }
                break; // break out of loop after the condition is found and the successes updated 
            } else if (i+1 >= conditions.length) {
                System.out.println("Condition not found: " + conditions[0]);
                return "ERROR: System error";
            }
        }
        return errorMessage;
    }

    private String isValidMin(int minIndex, String fieldQuestion, String fieldString, boolean list) {
        if (fieldString == null || fieldString.equals("")) return ""; //allow optionals
        int minCountNeeded = minFieldNs.get(minIndex);
        Field field = null;
        String errorMessage = "";
        try {
            if(list) {
                String[] fieldStringArray = fieldString.split(",");
                for (String fieldStr : fieldStringArray) {
                    System.out.println("Check each in list: " + fieldString);
                    field = Dataset.dataArray.get(Dataset.indexOfField(fieldStr));
                    if(minCountNeeded > StatisticalSummary.getCount(field.getValues())) throw new Exception(fieldStr);
                }
            } else {
                field = Dataset.dataArray.get(Dataset.indexOfField(fieldString));
                if(minCountNeeded > StatisticalSummary.getCount(field.getValues())) throw new Exception(fieldString);
            }
            System.out.println("Min values met: " + minCountNeeded + "\t" + StatisticalSummary.getCount(field.getValues()));
            return "";
        } catch (Exception e) { //Handles both if it's not a field and if it's N doesn't meet the minimum 
            errorMessage = "\"" + e.getMessage() + "\" must have at least " + minCountNeeded + " values for question \"" + fieldQuestion + "\"" ;
            return errorMessage;
        }
    }

    //Popup messages 
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",JOptionPane.ERROR_MESSAGE);
    }
    public static void showErrorMessage(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",JOptionPane.ERROR_MESSAGE);
    }
    public void showNoticeMessage(String message) {
        JOptionPane.showMessageDialog(parent, message, "Notice",JOptionPane.INFORMATION_MESSAGE);
    }
    public static void showNoticeMessage(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Notice",JOptionPane.INFORMATION_MESSAGE);
    }
}
