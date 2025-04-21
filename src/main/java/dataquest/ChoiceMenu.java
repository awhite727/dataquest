
package dataquest;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import dataquest.Popup.HandleError;
import dataquest.Popup.QuestionType;

public class ChoiceMenu {

    /* menu methods are statically called from layout
        new tests should have their own method that passes prompts and questions to a pop-up method
        methods that make pop-ups are private to encourage code reusability
    */
    public static String importingDelimMenu(JFrame parent) {
        final ArrayList<String> questionType = new ArrayList<String>(Arrays.asList("radio","text"));
        ArrayList<String[]> questionList = new ArrayList<String[]>(2);
        ArrayList<HandleError[]> errors = new ArrayList<>(2);
        questionList.add(new String[]{"File deliminator","Comma", "Semicolon", "Space", "New line", "Tab","Custom"});
        questionList.add(new String[]{"If Custom: "});
        errors.add(new HandleError[]{HandleError.REQUIRED});
        errors.add(new HandleError[]{HandleError.OPTIONAL_CONDITIONAL});

        Popup popup = new Popup(parent,"Select deliminator");
        //popup.addQuestion(QuestionType.BUBBLE,new String[]{"File deliminator: ","Comma", "Semicolon", "Space", "New line", "Tab","Custom"}, new HandleError[]{HandleError.NONE});
        popup.addQuestions(questionType, questionList, errors);
        popup.addOptionalConditional(0, "Custom");
        popup.printQuestions();
        String[] selected=  popup.showGenericPopup();//Popup.showGenericPopup(parent, "Deliminator", questionType, 
        //questionList, new ArrayList<>());
        System.out.println("SELECTED CALLED: " + Arrays.toString(selected));
        if(selected == null) return null;
        /* if((!selected[0].equals("Custom")) && selected[1].length() > 0) {
            System.out.println("ERROR: Custom deliminator used but " + selected[0] + " deliminator selected.");
            System.out.println("\t" + selected[1].length());
            return null;
        } */
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
                if(selected[1].length() > 1) {
                    Popup.showNoticeMessage(parent,"NOTICE: Your custom deliminator is will only separate on the full string or regex \"" + selected[1] + "\"");
                }
                return selected[1];
            default:
                System.out.println("ERROR: Unexpected error in importingDelimMenu");
                return null;
        }
    }

    public static void updateFieldType(JFrame parent) {
        final String tabName = "Update Field Type";
        final Field[] fields = Dataset.dataArray.toArray(new Field[Dataset.dataArray.size()]);
        String[] fieldNames = new String[fields.length+1];
        final String[] types = {"Type ", "String","Boolean","Numeric"};
        if (fields.length == 0) {
            System.out.println("ERROR: No fields to update");
            return;
        }
        fieldNames[0] = "Field ";
        
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i+1] = fields[i].getName();
            System.out.println("Added: " + fieldNames[i]);
        }

        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<HandleError[]> errors = new ArrayList<>();

        questionType.add("combo");
        questionList.add(fieldNames);
        errors.add(new HandleError[]{HandleError.REQUIRED});

        questionType.add("combo");
        questionList.add(types);
        errors.add(new HandleError[]{HandleError.REQUIRED});

        //String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);
        Popup popup = new Popup(parent, tabName);
        popup.addQuestions(questionType, questionList, errors);
        popup.printQuestions();
        String[] selected = popup.showGenericPopup();
        System.out.println(Arrays.toString(selected));
        if (selected==null) return;
        final Field field = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        boolean result;
        switch (selected[1]) {
            case "String":
                result = field.setType("String");
                break;
            case "Boolean":
            result = field.setType("boolean");
            break;
            case "Numeric":
            result = field.setType("float");
            break;        
            default: //Should not be reached
                System.out.println("ERROR: Not a valid type");
                return;
        }
        if(!result) {
            Popup.showNoticeMessage(parent, "Not all values match the selected type. These values have been hidden.");
        }
    }

    // set to void to avoid compilation errors, will return Visualization once finished.
    public static Visualization visualMenu(JFrame parent) {
        String[] visualOptions = {"Choose a visualization: ", "Boxplot", "Histogram", "Scatterplot", "T-Distribution"};

        String tabName = "Visualization";
        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("radio"));
        ArrayList<String[]> questionList = new ArrayList<>();
        questionList.add(visualOptions);
        ArrayList<HandleError[]> errors = new ArrayList<>();
        errors.add(new HandleError[]{HandleError.REQUIRED});  

        //String[] selected = Popup.showGenericPopup(parent, tabName, questionType, questionList, new ArrayList<>()); //Popup.genericPopup is more up to date
        Popup popup = new Popup(parent,tabName);
        popup.addQuestions(questionType, questionList, errors);
        String[] selected = popup.showGenericPopup();
        if (selected == null) return null;

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
        String [] fieldNames = new String[fields.length+1];
        fieldNames[0] = "Select a field: ";
        for (int i=0;i<fields.length;i++) {
            fieldNames[i+1] = fields[i].getName();
        }
        Popup popup = new Popup(parent,"Statistical Summary");
        popup.addQuestion(QuestionType.DROPDOWN, fieldNames, new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});
        popup.addMinFieldN(2); //Target Field must have at least 2 value 

        String[] selected = popup.showGenericPopup();//showComboPopup(parent, "Statistical Summary", "Choose a field", fieldNames);
        if (selected == null) return null;
        Field summaryField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
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
            Popup.showNoticeMessage(parent, "No missing to handle.");
            return;
        }
        fieldNamesList.add(0,"Fields: ");
        String[] options = new String[]{"Handle By: ", "Replace With Mean", "Replace With Median", "Forward Fill", "Backward Fill", "Omit Missing"};

        Popup popup = new Popup(parent, "Handle Missing Values");
        //require field and require that field has N of at least 1
        popup.addQuestion(QuestionType.DROPDOWN,(fieldNamesList.toArray(new String[fieldNamesList.size()])), new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});
        popup.addMinFieldN(1); 

        //require a method of handling
        popup.addQuestion(QuestionType.BUBBLE, options,new HandleError[]{HandleError.REQUIRED}); 

        String[] selected = popup.showGenericPopup();//showComboAndRadioPopup(parent, "Handle Missing Values", "Field", "Handle By", fieldNames, options);
        if(selected == null) return;
        String missingFieldName = selected[0];
        String missingMethod = selected[1];

        Field missingField = Dataset.dataArray.get(Dataset.indexOfField(missingFieldName));
        missingField.handleMissingValues(missingMethod);
    }

    public static String linearRegression(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        if (fields.length <= 1) {
            Popup.showNoticeMessage(parent,"Not enough numeric fields to perform linear regression.");
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
        ArrayList<HandleError[]> errors = new ArrayList<>();

        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N}); //Target field
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N}); //Predictor

        Popup popup = new Popup(parent, tabName);
        popup.addQuestions(questionType, questionList, errors);
        popup.addMinFieldN(1); //Target Field must have at least 1 value 
        popup.addMinFieldN(1); //Predictor Fields must each have at least 1 value
        
        String[] selected = popup.showGenericPopup();
        if (selected == null) return null;
        String targetName = selected[0];
        String[] parameterNames = selected[1].split(",");
        
        
        // get field
        Field target = Dataset.dataArray.get(Dataset.indexOfField(targetName));
        Field[] parameters = new Field[parameterNames.length];
        for (int i =0; i<parameterNames.length; i++) {
            parameters[i] = Dataset.dataArray.get(Dataset.indexOfField(parameterNames[i]));
        }

        String output = StatisticalSummary.getLinearRegression(target, parameters);
        if(output == null) {
            popup.showErrorMessage("Too many missing values to compute linear regression.");
        }
        return output;
    }

    public static Boxplot boxplotMenu(JFrame parent) {
        Field[] fields = Dataset.getNumericFields();
        Field[] levels = Dataset.getCategoricalFields();
        if (fields.length==0) {
            Popup.showNoticeMessage(parent,"No numerical fields to display.");
            return null;
        }
        String[] fieldNames = new String[fields.length+1];
        fieldNames[0] = "Choose a field: ";
        for (int i = 0; i<fields.length; i++) {
            fieldNames[i+1] = fields[i].getName();
        }
        String boxplotFieldName;
        Field boxplotField;
        Field categoryField = null;

        Popup popup = new Popup(parent, "Boxplot");
        popup.addQuestion(QuestionType.DROPDOWN, fieldNames,new HandleError[]{HandleError.REQUIRED}); //require field regardless
        /* if (levels.length==0) {
            boxplotFieldName = null;//showComboPopup(parent, "Boxplot", "Choose a field", fieldNames);
        } */
        if (levels.length > 0) {
            String[] levelNames = new String[levels.length + 1];
            //levelNames[0] = "None";
            levelNames[0] = "(Optional) Choose field for categories: ";
            for (int i = 1; i<levels.length+1; i++) {
                levelNames[i] = levels[i-1].getName();
            }
            popup.addQuestion(QuestionType.DROPDOWN, levelNames, new HandleError[]{HandleError.NONE});
        }
            String[] selected = popup.showGenericPopup();
            if(selected == null) return null;
            boxplotFieldName = selected[0];
        if(levels.length > 0) {
            if (selected[1].equalsIgnoreCase("")) {
                categoryField = null;
            }
            else {
                categoryField = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
            }
        }

        //TODO: Getting an error if the n of category is more than the n of field?
        //I can implement a field < field error detection, but if we need to force retangular for other reasons then no need to
        boxplotField = Dataset.dataArray.get(Dataset.indexOfField(boxplotFieldName));
        Boxplot boxplot = new Boxplot("Boxplot", null, boxplotField, categoryField);
        return boxplot;
    }

    //TODO: Fix error where non-retangular(?) x/y's have index error
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
        yFieldNames[0] = "Y Variable"; 
        for (int i = 0; i<fields.length; i++) {
            String name = fields[i].getName();
            xFieldNames[i + 1] = name;
            yFieldNames[i + 1] = name;
        }
        String [] categoryFieldNames = new String[categoryFields.length + 2];
        categoryFieldNames[0] = "(Optional) Level";
        //categoryFieldNames[1] = "none";
        for (int i = 0; i< categoryFields.length; i++) {
            categoryFieldNames[i+1] = categoryFields[i].getName();
        }
        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("combo", "combo", "combo"));
        ArrayList<String[]> questionList = new ArrayList<>(Arrays.asList(xFieldNames, yFieldNames, categoryFieldNames));
        ArrayList<HandleError[]> errors = new ArrayList<>();
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N}); //xField
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N}); //yField
        errors.add(new HandleError[]{HandleError.MINIMUM_FIELD_N}); //categoryField
        
        
        Popup popup = new Popup(parent,"Scatterplot");
        popup.addQuestions(questionType, questionList, errors);
        popup.addMinFieldN(1); //xField
        popup.addMinFieldN(1); //yField
        popup.addMinFieldN(1); //categoryField



        String[] selected = popup.showGenericPopup();
        if (selected == null) return null;

        Field xField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field yField = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        String categoryFieldName = selected[2];
        Graph graph;
        if (categoryFieldName == null) {
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

    //TODO: Add from one-sample from data (backend done) 
    public static TDistribution tDistributionMenu(JFrame parent) {

        ArrayList<String> questionType = new ArrayList<>(Arrays.asList("text", "radio", "text"));
        String [] dfQuestion = new String[] {"Degrees of Freedom"};
        String [] directionQuestion = new String[] {"Direction", "Upper Tail", "Lower Tail", "Two-Tailed", "Confidence Interval"};
        String [] aQuestion = new String[] {"T-Score"};
        ArrayList<String[]> questionList = new ArrayList<>(Arrays.asList(dfQuestion, directionQuestion, aQuestion));
        ArrayList<HandleError[]> errors = new ArrayList<>();
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.INTEGER,HandleError.POSITIVE_VALUE}); //df
        errors.add(new HandleError[]{HandleError.REQUIRED}); //Direction
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.NUMERIC}); //t-score

        Popup popup = new Popup(parent,"T-Distribution");
        popup.addQuestions(questionType, questionList, errors); 
        String[] selected = popup.showGenericPopup();//Popup.showGenericPopup(parent, "T-Distribution", questionType, questionList, new ArrayList<String[]>());

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
        String[] fieldNames = new String[fields.length+1];
        fieldNames[0] = "Test field ";
        for (int i = 0; i<fields.length; i++) {
            fieldNames[i+1] = fields[i].getName();
        }
        String[] levelNames = new String[levels.length+1];
        levelNames[0] = "Level ";
        for (int i=0; i<levels.length; i++) {
            levelNames[i+1] = levels[i].getName();
        }
        Popup popup = new Popup(parent,"ANOVA");
        popup.addQuestion(QuestionType.DROPDOWN,fieldNames,new HandleError[]{HandleError.REQUIRED});
        popup.addQuestion(QuestionType.DROPDOWN,levelNames,new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});
        
        popup.addMinFieldN(2); //level needs at least 2 values in field (doesn't fully check for possibility of index, just makes sure that there's a possibility)

        String[] selected = popup.showGenericPopup();//showTwoComboPopup(parent, "ANOVA", "Test field", "Level", fieldNames, levelNames);
        if (selected == null) return null;
        Field numField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field levField = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        return StatisticalSummary.getAnova(numField, levField);
    }
 
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
        ArrayList<String> qTypes = new ArrayList<>(Arrays.asList("combo","radio","text"));
        ArrayList<String[]> qList = new ArrayList<>(Arrays.asList(fieldNames,shapeBy, new String[]{"Input: "}));
        ArrayList<HandleError[]> errors = new ArrayList<>();

        errors.add(new HandleError[]{HandleError.REQUIRED}); //field
        errors.add(new HandleError[]{HandleError.REQUIRED}); //bubble choice
        errors.add(new HandleError[]{HandleError.REQUIRED, HandleError.CONDITIONAL_ERROR}); //textbox
        ArrayList<Object[]> conditionalChecks = new ArrayList<>();
        conditionalChecks.add(new Object[]{binSize,HandleError.POSITIVE_VALUE});
        conditionalChecks.add(new Object[]{numBins,HandleError.INTEGER,HandleError.POSITIVE_VALUE});


        Popup popup = new Popup(parent, "Histogram");
        popup.addQuestions(qTypes, qList, errors);
        popup.addConditionalErrors(1, conditionalChecks);

        String[] selected = popup.showGenericPopup(); 
        if(selected == null) return null;
                
        Field summaryField = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Histogram histogram = new Histogram((summaryField.getName() + " Histogram"), null, summaryField);
        
        if(selected[1].equals(binSize)) {
            histogram.setBins(false,Double.valueOf(selected[2]));
        } else {
            histogram.setBins(true,Integer.valueOf(selected[2]));
        }/* 
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
        }  */
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
            Popup.showNoticeMessage(parent, "Not enough numerical fields available for mean comparison.");
            return null;
        }
        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<HandleError[]> errors = new ArrayList<>();
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        String [] fieldB = new String[fields.length+1];
        fieldA[0] = "Field A: ";
        fieldB[0] = "Field B: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
            fieldB[i+1] = fields[i].getName();
        } //TODO: note that tabbedPane might be handy for popups or something maybe for like the graph menus? 

        Popup popup = new Popup(parent, tabName);
        //Fields
        questionType.add("combo");
        questionList.add(fieldA);
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});

        questionType.add("combo");
        questionList.add(fieldB);
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});

        //Hypothesis 
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        
        questionType.add("comparison");
        questionList.add(new String[]{"Field A - Field B    ","<",">","=","    d"}); //\u2260 is uncode for !=   
        errors.add(new HandleError[]{HandleError.REQUIRED});
        
        //difference
        questionType.add("text");
        questionList.add(new String[]{"d = "});
        errors.add(new HandleError[]{HandleError.NUMERIC});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0)"});
        
        //alpha
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "});
        errors.add(new HandleError[]{HandleError.ZERO_TO_ONE});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        
        //variance?
        questionType.add("radio");
        questionList.add(new String[]{"Assume equal variance?","yes (Student/Pooled t-test)", "no (Welch t-test)"});       
        errors.add(new HandleError[]{HandleError.REQUIRED});
        
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        popup.addQuestions(questionType, questionList, errors);
        popup.addMinFieldN(2); //for fieldA
        popup.addMinFieldN(2); //for fieldB

        popup.printQuestions();
        String[] selected = popup.showGenericPopup();
        //String selected[] = Popup.showGenericPopup(parent, tabName, questionType, questionList, new ArrayList<String[]>());

        //Check validity, if anything null return 
        /* for (String string : selected) {
            if (string == null) return "";
        } */
        if(selected == null) return null;
        
        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        
        Direction direction;
        switch (selected[2]) {
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
                System.out.println("ERROR: Not a valid direction: " + selected[2]);
                return "";
        }
        double difference = 0;
        double alpha = 0.05;
        if(!selected[3].strip().equals("")) difference = Double.valueOf(selected[3]);
        if(!selected[4].strip().equals("")) alpha = Double.valueOf(selected[4]);

        String test = selected[5];
        TwoSample tt = new TwoSample(fieldOne, fieldTwo, alpha, difference, direction);
        if(test.equals("yes (Student/Pooled t-test)")) {
            tt.setPooled();
        } else if(test.equals("no (Welch t-test)")) {
            tt.setWelch();
        } else {
            System.out.println("ERROR: Test not found");
            return null;
        }
        return tt.printBasic();
    }

    public static String pairedMenu(JFrame parent) {
        final String tabName = "Paired Sample T-Test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            Popup.showNoticeMessage(parent,"Not enough numerical fields available for mean comparison.");
            return null;
        }
        /* ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<HandleError[]> errors = new ArrayList<>();
         */
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        String [] fieldB = new String[fields.length+1];
        fieldA[0] = "Field A: ";
        fieldB[0] = "Field B: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
            fieldB[i+1] = fields[i].getName();
        }
        
        Popup popup = new Popup(parent, tabName);
        //Fields
        popup.addQuestion(QuestionType.DROPDOWN, fieldA, new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});
        popup.addMinFieldN(2); //for fieldA
        popup.addQuestion(QuestionType.DROPDOWN, fieldB, new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});
        popup.addMinFieldN(2); //for fieldB

        //Hypothesis 
        popup.addQuestion(QuestionType.LABEL, new String[]{"H\u2080:"}, null);// \u2080 is unicode for subscript 0
        popup.addQuestion(QuestionType.COMPARISON, new String[]{"Field A - Field B    ","<",">","=","    d"}, new HandleError[]{HandleError.REQUIRED});
        
        //Difference
        popup.addQuestion(QuestionType.TEXT,new String[]{"d = "},new HandleError[]{HandleError.POSITIVE_VALUE});
        popup.addQuestion(QuestionType.LABEL, new String[]{"(default: 0)"}, null);
        //popup.addQuestion(QuestionType.LABEL, new String[]{"\n"}, null); 
        popup.addQuestion(QuestionType.TEXT,new String[]{"\u03B1 = "},new HandleError[]{HandleError.ZERO_TO_ONE});
        popup.addQuestion(QuestionType.LABEL, new String[]{"(default: 0.05)"}, null); 
        //popup.addQuestion(QuestionType.LABEL, new String[]{"\n"}, null);

        String selected[] = popup.showGenericPopup();//Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);
        if (selected == null) return null;

        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));

        Direction direction;
        switch (selected[2]) {
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
                System.out.println("ERROR: Not a valid direction: " + selected[2]);
                return "";
        }
        double difference = 0;
        double alpha = 0.05;

        if(!selected[3].equals("")) difference = Double.parseDouble(selected[3]); //valueOf? 

        if(!selected[4].strip().equals("")) alpha = Double.parseDouble(selected[4]);
        
        TwoSample tt = new TwoSample(fieldOne, fieldTwo, alpha, difference, direction);
        if(!tt.setPaired()) {
            Popup.showErrorMessage(parent, "Not enough pairs to calculate");
            return null;
        }
        return tt.printBasic();
    }

    //TODO: Found a weird error scrolling up and down? Sometimes textfields just? disappeared? but scrolling up and down again made them reappear? 
    public static String zTwoSampleMenu(JFrame parent) {
        final String tabName = "Two Sample Z-Test";
        Field[] fields = Dataset.getNumericFields();
        if (fields.length < 2) {
            System.out.println("ERROR: Not enough numerical fields available for mean comparison.");
            return "";
        }
        ArrayList<String> questionType = new ArrayList<>();
        ArrayList<String[]> questionList = new ArrayList<>();
        ArrayList<HandleError[]> errors = new ArrayList<>();
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
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});

        questionType.add("combo");
        questionList.add(fieldB);
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});

        //Get SDs 
        questionType.add("text");
        questionList.add(new String[]{"Field A \u03C3 = "}); //population sd sigma
        errors.add(new HandleError[]{HandleError.POSITIVE_VALUE,HandleError.REQUIRED,HandleError.CUSTOM_ERROR_MESSAGE}); 

        questionType.add("text");
        questionList.add(new String[]{"Field B \u03C3 = "}); //population sd sigma
        errors.add(new HandleError[]{HandleError.POSITIVE_VALUE,HandleError.REQUIRED,HandleError.CUSTOM_ERROR_MESSAGE}); 
        
        //Hypothesis 
        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0

        questionType.add("comparison");
        questionList.add(new String[]{"Field A - Field B    ","<",">","=","    d"}); //\u2260 is uncode for !=         
        errors.add(new HandleError[]{HandleError.REQUIRED});

        questionType.add("text");
        questionList.add(new String[]{"d = "});
        errors.add(new HandleError[]{HandleError.NUMERIC});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "});
        questionType.add("label");
        errors.add(new HandleError[]{HandleError.ZERO_TO_ONE});
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        Popup popup = new Popup(parent, tabName);
        popup.addQuestions(questionType, questionList,errors); 
        popup.addMinFieldN(2); //for fieldA
        popup.addMinFieldN(2); //for fieldB
        popup.addCustomMessage("If population SD unknown, use a t-test");
        popup.addCustomMessage("If population SD unknown, use a t-test");
        popup.printQuestions();
        String selected[] = popup.showGenericPopup();//Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);
        if (selected == null) return null;

        if (selected[2] == "" || selected[3] == "") {
            System.out.println("ERROR: Population SDs required for a z-test. If unknown, try a two sample t-test");
        }
        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));
        double sdA = Double.valueOf(selected[2]);  
        double sdB = Double.valueOf(selected[3]); 
        
        String directionCheck = selected[4];
        double difference = 0; //selected[5]
        double alpha = 0.05; // selected[6];
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
        if(!selected[5].strip().equals("")) difference = Double.valueOf(selected[5]);
            
        if(!selected[6].strip().equals("")) alpha = Double.valueOf(selected[6]);

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
        ArrayList<HandleError[]> errors = new ArrayList<>();
        questionType.add("radio");
        questionList.add(new String[]{"What type of proportion test would you like to perform?", "One-Sample", "Two-Sample"});
        errors.add(new HandleError[]{HandleError.REQUIRED});

        Popup popup = new Popup(parent, tabName);
        popup.addQuestions(questionType, questionList, errors);
        String selected[] = popup.showGenericPopup();//Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);
        if(selected == null) return null;
        else if(selected[0].equals("One-Sample")) return oneProportionMenu(parent);
        else if(selected[0].equals("Two-Sample")) return twoProportionMenu(parent);
        else return null;
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
        ArrayList<HandleError[]> errors = new ArrayList<>();
        //Prepare for popup
        String [] fieldA = new String[fields.length+1];
        fieldA[0] = "Field: ";
        for (int i=0;i<fields.length;i++) {
            fieldA[i+1] = fields[i].getName();
        }

        //Fields and success checkers
        questionType.add("combo");
        questionList.add(fieldA); 
        errors.add(new HandleError[]{HandleError.REQUIRED,HandleError.MINIMUM_FIELD_N});

        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("label");
        questionList.add(new String[]{"Success parameters"});

        questionType.add("comparison");
        questionList.add(new String[]{"x    ","<",">","=","    success"}); //\u2260 is uncode for !=      
        errors.add(new HandleError[]{HandleError.REQUIRED});   
        questionType.add("text");
        questionList.add(new String[]{"success = "});
        errors.add(new HandleError[]{HandleError.REQUIRED, HandleError.NUMERIC});
        
        //Hypothesis
        questionType.add("label");
        questionList.add(new String[]{"\n"}); 
        questionType.add("label");
        questionList.add(new String[]{"Hypothesis"});
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0
        
        questionType.add("comparison");
        questionList.add(new String[]{"P\u2081    ","<",">","=","    P\u2080"}); 
        errors.add(new HandleError[]{HandleError.REQUIRED});

        questionType.add("text");
        questionList.add(new String[]{"P\u2080 = "}); // Pnull
        errors.add(new HandleError[] {HandleError.REQUIRED,HandleError.ZERO_TO_ONE});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "}); // alpha
        errors.add(new HandleError[] {HandleError.ZERO_TO_ONE});
        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        Popup popup = new Popup(parent, tabName);
        popup.addQuestions(questionType, questionList, errors);
        popup.addMinFieldN(2);
        String selected[] = popup.showGenericPopup();//Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);
        if (selected == null) return null;

        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Direction successDirection;
        switch (selected[1]) {
            case "<":
                successDirection = Direction.LESS_THAN;
                break;
            case ">":
                successDirection = Direction.GREATER_THAN;
                break;
            case "=":
                successDirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + selected[1]);
                return null;
        }

        double successBound = Double.valueOf(selected[2]);
        
        Direction nullDirection;
        switch (selected[3]) {
            case "<":
                nullDirection = Direction.LESS_THAN; 
                break;
            case ">":
                nullDirection = Direction.GREATER_THAN;
                break;
            case "=":
                nullDirection = Direction.EQUAL;
                break;
            default:
                System.out.println("ERROR: Not a valid direction: " + selected[3]);
                return "";
        }

        double pNull = Double.valueOf(selected[4]); //Optional?
        double alpha = 0.05; //selected[5]
        

        if(!selected[5].strip().equals("")) {
            try {
                alpha = Double.valueOf(selected[5]); //valueOf? 
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
        ArrayList<HandleError[]> errors = new ArrayList<>();
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
        errors.add(new HandleError[]{HandleError.REQUIRED, HandleError.MINIMUM_FIELD_N});
        questionType.add("combo");
        questionList.add(fieldB); 
        errors.add(new HandleError[]{HandleError.REQUIRED, HandleError.MINIMUM_FIELD_N});

        questionType.add("label");
        questionList.add(new String[]{"\n"});
        questionType.add("label");
        questionList.add(new String[]{"Success parameters"});

        questionType.add("comparison");
        questionList.add(new String[]{"FieldA(x)    ","<",">","=","    success"}); //\u2260 is uncode for !=   
        errors.add(new HandleError[]{HandleError.REQUIRED});
      
        questionType.add("text");
        questionList.add(new String[]{"success = "});
        errors.add(new HandleError[]{HandleError.REQUIRED, HandleError.NUMERIC});

        questionType.add("comparison");
        questionList.add(new String[]{"FieldB(x)    ","<",">","=","    success"}); //\u2260 is uncode for !=  
        errors.add(new HandleError[]{HandleError.REQUIRED});
       
        questionType.add("text");
        questionList.add(new String[]{"success = "});
        errors.add(new HandleError[]{HandleError.REQUIRED, HandleError.NUMERIC});

        
        //Hypothesis
        questionType.add("label");
        questionList.add(new String[]{"\n"}); 
        questionType.add("label");
        questionList.add(new String[]{"Hypothesis"});
        questionType.add("label");
        questionList.add(new String[]{"H\u2080:"}); // \u2080 is unicode for subscript 0

        questionType.add("comparison");
        //questionList.add(new String[]{"\u0070\u0302    ","<",">","=","    P\u2080"}); //phat <>= PNull //\u2260 is uncode for !=        
        questionList.add(new String[]{"P\u2081    ","<",">","=","    P\u2082"});
        errors.add(new HandleError[]{HandleError.REQUIRED});

        questionType.add("label");
        questionList.add(new String[]{"\n"});
        
        questionType.add("text");
        questionList.add(new String[]{"\u03B1 = "}); // alpha
        errors.add(new HandleError[]{HandleError.ZERO_TO_ONE});

        questionType.add("label");
        questionList.add(new String[]{"(default: 0.05)"});
        questionType.add("label");
        questionList.add(new String[]{"\n"});

        Popup popup = new Popup(parent, tabName);
        popup.addQuestions(questionType, questionList, errors);
        popup.addMinFieldN(2);
        popup.addMinFieldN(2);

        String selected[] = popup.showGenericPopup();//Popup.showGenericPopup(parent, tabName, questionType, questionList, errors);

        if (selected == null) return null;

        Field fieldOne = Dataset.dataArray.get(Dataset.indexOfField(selected[0]));
        Field fieldTwo = Dataset.dataArray.get(Dataset.indexOfField(selected[1]));

        String successADirectionCheck = selected[2];
        double successABound = Double.valueOf(selected[3]);
        
        String successBDirectionCheck = selected[4];
        double successBBound = Double.valueOf(selected[5]);
        String nullDirectionCheck = selected[6];
        double alpha = 0.05;
        if(!selected[7].strip().equals("")) alpha = Double.valueOf(selected[7]);
        
        Direction successADirection;
        switch (successADirectionCheck) {
            case "<":
                successADirection = Direction.LESS_THAN; 
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
        
        Direction successBDirection;
        switch (successBDirectionCheck) {
            case "<":
                successBDirection = Direction.LESS_THAN; 
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
        
        Direction nullDirection;
        switch (nullDirectionCheck) {
            case "<":
                nullDirection = Direction.LESS_THAN; 
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

        Proportion proportion = new Proportion(fieldOne, fieldTwo, successABound, successADirection, successBBound, successBDirection, alpha, nullDirection);
        proportion.setTwoProportion();
        return proportion.printBasic();
    }
}
