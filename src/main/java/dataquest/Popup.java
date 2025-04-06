package dataquest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SizeRequirements;

//TODO: Use class to break down each element into a private set and get of the Component
//setComponent should run any necessary checks for that component then return the Component
//getComponent show run necessary submission checks for that component then return the associated String
//TODO: Fix automatic width sizing
//TODO: Add error handling 
//TODO: Add next/previous/submit
//TODO: Add tabbed pane 
//TODO: Add text wrapping 
    //-- JLabel.setText("<html>Text</html>")/new JLabel("<html>" + text + "</html>")
    //Special characters need to be escaped first, like \<
    //https://stackoverflow.com/questions/2420742/make-a-jlabel-wrap-its-text-by-setting-a-max-width 
public class Popup { 
    Popup(){}
    //NOTE: If we want to include generic error handling prior to submission, best way to do this I can think of would be to handle by passing another ArrayList<String[]> errors
    //Each String[] would contain String index of what to check for errors, a check type (i.e. "int", "lessThan", "exactMatch" etc) at index 1 and the specification at index 2 ("2", "I accept these terms and conditions")
    //New private methods would be added to be called by a checker





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
                    if(qType.equals("comparison")) {
                        
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
        return selectedValues;
    }
}
