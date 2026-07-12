package main.java.visualizer.ui;

import javax.swing.*;
import java.awt.*;

public class TestUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Тест интерфейса визуализатора");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Создаем панель графа (Выполняет пункты 1 и 2)
            GraphPanel graphPanel = new GraphPanel();
            frame.add(graphPanel, BorderLayout.CENTER);

            // Нижняя панель с модальными кнопками
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

            JButton errBtn = new JButton("Тест Ошибки");
            errBtn.addActionListener(e -> MessageDialog.show(frame, "Ошибка алгоритма", "В графе обнаружен цикл отрицательного веса!"));

            JButton aboutBtn = new JButton("О разработчиках");
            aboutBtn.addActionListener(e -> AboutDialog.show(frame));

            JButton vertexBtn = new JButton("Создать Вершину");
            vertexBtn.addActionListener(e -> {
                VertexDialog dialog = new VertexDialog(frame);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    System.out.println("Создана вершина: " + dialog.getVertexName() + " (" + dialog.getVertexX() + ", " + dialog.getVertexY() + ")");
                }
            });


            controlPanel.add(errBtn);
            controlPanel.add(aboutBtn);
            controlPanel.add(vertexBtn);
            
            frame.add(controlPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        });
    }
}
