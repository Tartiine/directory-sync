package directorysync;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Interface {

    //The components
    JFrame frame = new JFrame("DirSync");
    
    JPanel panel_srcPath = new JPanel();
    JLabel label_srcPath = new JLabel("Source directory (path): ");
    JTextField srcPath = new JTextField(10);

    JPanel panel_trgtPath = new JPanel();
    JLabel label_trgtPath = new JLabel("Target directory (path): ");
    JTextField trgtPath = new JTextField(10);

    JPanel panel_syncButtons = new JPanel();
    JButton button_softSync = new JButton("Soft Synchronisation");
    JButton button_hardSync = new JButton("Hard Synchronisation");
    JButton button_mutualSync = new JButton("Mutual synchronisation (ignore conflicts)");

    JLabel label_output = new JLabel("Output:");
    JLabel output = new JLabel("");

    public void buildInterface (Interface interf) {
        //Places the components inside each other and displays the interface frame
        panel_srcPath.add(label_srcPath);
        panel_srcPath.add(srcPath);
        frame.add(panel_srcPath);
        
        panel_trgtPath.add(label_trgtPath);
        panel_trgtPath.add(trgtPath);
        frame.add(panel_trgtPath);
        
        panel_syncButtons.add(button_softSync);
        panel_syncButtons.add(button_hardSync);
        frame.add(panel_syncButtons);

        frame.add(button_mutualSync);
        frame.add(label_output);
        frame.add(output);
        frame.setMinimumSize(new Dimension((int) frame.getMinimumSize().getWidth(), 450));
        
        frame.setLayout(new GridLayout(6, 1));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //Affects the events to the buttons
        button_hardSync.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                output.setText(
                    "<html>Starting hard synchronisation<br>"
                    + "from: " + srcPath.getText() + "<br>"
                    + "to: " + trgtPath.getText() + "<br>"
                    + "Hard synchronisation complete!</html>");
            }
        });
        button_softSync.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                output.setText(
                    "<html>Starting soft synchronisation<br>"
                    + "from: " + srcPath.getText() + "<br>"
                    + "to: " + trgtPath.getText() + "<br>"
                    + "Soft synchronisation complete!</html>");
            }
        });
        button_mutualSync.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                output.setText(
                    "<html>Starting mutual synchronisation<br>"
                    + "Mutual synchronisation complete!</html>");
            }
        });
    }

    public static void main(String[] args) {
        Interface interf = new Interface();
        interf.buildInterface(interf);
    }
}