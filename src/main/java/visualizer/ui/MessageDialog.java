package visualizer.ui;

import javax.swing.*;
import java.awt.*;

public class MessageDialog {
    public static void show(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(
            parent, 
            message, 
            title, 
            JOptionPane.ERROR_MESSAGE
        );
    }
}
