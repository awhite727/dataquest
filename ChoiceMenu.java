import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class ChoiceMenu extends JFrame {
    private JButton openButton;
    private JDialog popup;

    public ChoiceMenu() {
        // Set up the main window
        setTitle("Main Window");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Create the button to open the popup
        openButton = new JButton("Choice Menu");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup();
            }
        });
        add(openButton);

        // Create the popup window
        popup = new JDialog(this, "Choice Menu", true);
        popup.setSize(250, 150);
        popup.setLayout(new FlowLayout());
        
        // Add placeholder text to the popup
        JLabel placeholderText = new JLabel("Placeholder text for future content.");
        popup.add(placeholderText);

        // Add a close button to the popup
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popup.setVisible(false);
            }
        });
        popup.add(closeButton);
    }

    private void showPopup() {
        // Center the popup relative to the main window
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
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