package main.java.visualizer.ui;

import javax.swing.*;
import java.awt.*;

public class VertexDialog extends JDialog {
    private JTextField nameField;
    private JTextField xField;
    private JTextField yField;
    
    private boolean confirmed = false; // Флаг: сохранил ли пользователь данные

    public VertexDialog(Frame parent) {
        super(parent, "Управление вершиной", true); // true делает окно модальным
        setLayout(new BorderLayout(10, 10));
        setSize(350, 220);
        setLocationRelativeTo(parent);

        // Панель формы ввода
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Имя вершины:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Координата X (пиксели):"));
        xField = new JTextField();
        formPanel.add(xField);

        formPanel.add(new JLabel("Координата Y (пиксели):"));
        yField = new JTextField();
        formPanel.add(yField);

        // Панель кнопок управления
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Сохранить Vertex");
        JButton cancelBtn = new JButton("Отмена");

        // Действие при нажатии кнопки "Сохранить"
        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Имя вершины не может быть пустым!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            setVisible(false); // Закрываем окно
        });

        // Действие при нажатии кнопки "Отмена"
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Геттеры для сбора данных, введенных пользователем
    public boolean isConfirmed() { return confirmed; }
    public String getVertexName() { return nameField.getText().trim(); }
    public String getVertexX() { return xField.getText().trim(); }
    public String getVertexY() { return yField.getText().trim(); }
}
