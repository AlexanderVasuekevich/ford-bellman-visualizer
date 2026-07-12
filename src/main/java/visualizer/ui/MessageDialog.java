package main.java.visualizer.ui;// Замени на свой пакет, если папка называется по-другому

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
