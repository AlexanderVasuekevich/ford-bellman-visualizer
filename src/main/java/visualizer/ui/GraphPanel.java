package main.java.visualizer.ui;

import javax.swing.*;
import java.awt.*;

public class GraphPanel extends JPanel {
    private static final int VERTEX_RADIUS = 20;

    // Данные для макета
    private final Object[][] vertices = {
        {"A", 400, 240},
        {"B", 240, 390},
        {"C", 80, 240},
        {"D", 240, 70}
    };

    // Ребра графа: {Индекс_Старта, Индекс_Конца, Вес}
    private final int[][] edges = {
        {2, 3, 3},  
        {2, 0, 8},  
        {2, 1, -2},  
        {1, 0, 5}    
    };
    private int currentProcessingEdgeIndex = 1;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < edges.length; i++) {
            boolean isActive = (i == currentProcessingEdgeIndex);
            drawEdge(g2d, i, isActive);
        }

        for (int i = 0; i < vertices.length; i++) {
            boolean isActiveVertex = false;

            // Проверяем, принадлежит ли вершина выделенному ребру
            if (currentProcessingEdgeIndex >= 0 && currentProcessingEdgeIndex < edges.length) {
                int startVertex = edges[currentProcessingEdgeIndex][0];
                int endVertex = edges[currentProcessingEdgeIndex][1];
                if (i == startVertex || i == endVertex) {
                    isActiveVertex = true;
                }
            }

            drawVertex(g2d, i, isActiveVertex);
        }
    }

    private void drawEdge(Graphics2D g2d, int edgeIndex, boolean isActive) {
        int startIdx = edges[edgeIndex][0];
        int endIdx = edges[edgeIndex][1];
        int weight = edges[edgeIndex][2];

        int x1 = (int) vertices[startIdx][1];
        int y1 = (int) vertices[startIdx][2];
        int x2 = (int) vertices[endIdx][1];
        int y2 = (int) vertices[endIdx][2];

        if (isActive) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3.0f)); // Жирный контур
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.0f)); // Обычный контур
        }

        g2d.drawLine(x1, y1, x2, y2);

        double angle = Math.atan2(y2 - y1, x2 - x1);

        int arrowX = x2 - (int) (VERTEX_RADIUS * Math.cos(angle));
        int arrowY = y2 - (int) (VERTEX_RADIUS * Math.sin(angle));
        
        int arrowSize = 8;
        int x3 = arrowX - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = arrowY - (int) (arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = arrowX - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = arrowY - (int) (arrowSize * Math.sin(angle + Math.PI / 6));
        
        g2d.drawLine(arrowX, arrowY, x3, y3);
        g2d.drawLine(arrowX, arrowY, x4, y4);

        int midX = (x1 + x2) / 2 + 10;
        int midY = (y1 + y2) / 2 - 10;
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2d.drawString(String.valueOf(weight), midX, midY);
    }

    private void drawVertex(Graphics2D g2d, int vertexIndex, boolean isActive) {
        String name = (String) vertices[vertexIndex][0];
        int x = (int) vertices[vertexIndex][1];
        int y = (int) vertices[vertexIndex][2];

        int ovalX = x - VERTEX_RADIUS;
        int ovalY = y - VERTEX_RADIUS;
        int diameter = VERTEX_RADIUS * 2;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(ovalX, ovalY, diameter, diameter);

        if (isActive) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3.0f));
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.0f));
        }
        g2d.drawOval(ovalX, ovalY, diameter, diameter);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2d.drawString(name, x, y);
    }
}
