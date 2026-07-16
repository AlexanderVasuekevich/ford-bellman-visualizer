package visualizer.ui;

import visualizer.model.Edge;
import visualizer.model.Graph;
import visualizer.model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {
    private static final int VERTEX_RADIUS = 25;
    
    private Graph graph;
    private List<Vertex> vertexList;
    private List<Edge> edgeList;
    private int currentProcessingEdgeIndex = -1;
    private String updatedVertexName = null; // Вершина, у которой обновилось расстояние
    private Map<String, Integer> distances;
    private Map<String, String> predecessors;
    private boolean showDistances = false;

    public GraphPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        this.graph = null;
        this.vertexList = new ArrayList<>();
        this.edgeList = new ArrayList<>();
    }

    public GraphPanel(Graph graph) {
        this();
        setGraph(graph);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        if (graph != null) {
            this.vertexList = new ArrayList<>(graph.getVertices());
            this.edgeList = graph.getEdges();
            autoLayoutVertices();
        }
        repaint();
    }

    // Размещение вершин по кругу
    private void autoLayoutVertices() {
        if (vertexList == null || vertexList.size() < 2) {
            if (vertexList != null && vertexList.size() == 1) {
                Vertex v = vertexList.get(0);
                v.setX(400);
                v.setY(300);
            }
            return;
        }

        int count = vertexList.size();
        int centerX = 400;
        int centerY = 300;
        int radius = Math.min(centerX, centerY) - 60;

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count - Math.PI / 2;
            Vertex v = vertexList.get(i);
            v.setX(centerX + (int)(radius * Math.cos(angle)));
            v.setY(centerY + (int)(radius * Math.sin(angle)));
        }
    }

    public void setCurrentProcessingEdge(int edgeIndex) {
        this.currentProcessingEdgeIndex = edgeIndex;
        repaint();
    }

    public void setUpdatedVertex(String vertexName) {
        this.updatedVertexName = vertexName;
        repaint();
    }

    public void setDistances(Map<String, Integer> distances) {
        this.distances = distances;
        this.showDistances = true;
        repaint();
    }

    public void setPredecessors(Map<String, String> predecessors) {
        this.predecessors = predecessors;
        repaint();
    }

    public void clearHighlight() {
        this.currentProcessingEdgeIndex = -1;
        this.updatedVertexName = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (vertexList == null || vertexList.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Dialog", Font.BOLD, 16));
            g2d.drawString("Нет данных для отображения", 300, 300);
            return;
        }

        // Рисуем ребра
        for (int i = 0; i < edgeList.size(); i++) {
            boolean isActive = (i == currentProcessingEdgeIndex);
            drawEdge(g2d, edgeList.get(i), isActive);
        }

        // Рисуем вершины
        for (Vertex v : vertexList) {
            boolean isFrom = false;
            boolean isTo = false;
            boolean isUpdated = false;
            
            if (currentProcessingEdgeIndex >= 0 && currentProcessingEdgeIndex < edgeList.size()) {
                Edge edge = edgeList.get(currentProcessingEdgeIndex);
                isFrom = v.equals(edge.getFrom());
                isTo = v.equals(edge.getTo());
            }
            
            if (updatedVertexName != null && v.getName().equals(updatedVertexName)) {
                isUpdated = true;
            }
            
            drawVertex(g2d, v, isFrom, isTo, isUpdated);
        }
    }

    private void drawEdge(Graphics2D g2d, Edge edge, boolean isActive) {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();
        int weight = edge.getWeight();

        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();

        // Вычисляем точки на окружности вершины
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int startX = x1 + (int)(VERTEX_RADIUS * Math.cos(angle));
        int startY = y1 + (int)(VERTEX_RADIUS * Math.sin(angle));
        int endX = x2 - (int)(VERTEX_RADIUS * Math.cos(angle));
        int endY = y2 - (int)(VERTEX_RADIUS * Math.sin(angle));

        // Настройка стиля линии
        if (isActive) {
            g2d.setColor(new Color(255, 0, 0)); // Красный - текущее ребро
            g2d.setStroke(new BasicStroke(4.0f));
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2.0f));
        }

        // Рисуем линию
        g2d.drawLine(startX, startY, endX, endY);

        // Рисуем стрелку
        int arrowSize = 12;
        int x3 = endX - (int)(arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = endY - (int)(arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = endX - (int)(arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = endY - (int)(arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.fillPolygon(new int[]{endX, x3, x4}, new int[]{endY, y3, y4}, 3);

        // Рисуем вес ребра
        int midX = (x1 + x2) / 2 + 15;
        int midY = (y1 + y2) / 2 - 15;
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(midX - 12, midY - 10, 24, 20);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawOval(midX - 12, midY - 10, 24, 20);
        
        g2d.setFont(new Font("Dialog", Font.BOLD, 14));
        g2d.drawString(String.valueOf(weight), midX - 6, midY + 5);
    }

    private void drawVertex(Graphics2D g2d, Vertex vertex, boolean isFrom, boolean isTo, boolean isUpdated) {
        String name = vertex.getName();
        int x = vertex.getX();
        int y = vertex.getY();

        int ovalX = x - VERTEX_RADIUS;
        int ovalY = y - VERTEX_RADIUS;
        int diameter = VERTEX_RADIUS * 2;

        // Тень
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillOval(ovalX + 3, ovalY + 3, diameter, diameter);

        // Цвет заливки в зависимости от состояния
        Color fillColor = Color.WHITE;
        Color borderColor = Color.BLACK;
        float borderWidth = 2.0f;

        if (isUpdated) {
            // Обновленная вершина - зеленый
            fillColor = new Color(144, 238, 144); // Светло-зеленый
            borderColor = new Color(0, 150, 0);
            borderWidth = 4.0f;
        } else if (isFrom) {
            // Начальная вершина - оранжевый
            fillColor = new Color(255, 200, 100);
            borderColor = new Color(255, 140, 0);
            borderWidth = 3.0f;
        } else if (isTo) {
            // Конечная вершина - желтый
            fillColor = new Color(255, 255, 150);
            borderColor = new Color(200, 200, 0);
            borderWidth = 3.0f;
        } else if (distances != null && distances.containsKey(name) && distances.get(name) == 0) {
            // Стартовая вершина
            fillColor = new Color(200, 255, 200);
            borderColor = new Color(0, 150, 0);
        }

        g2d.setColor(fillColor);
        g2d.fillOval(ovalX, ovalY, diameter, diameter);

        // Контур
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(borderWidth));
        g2d.drawOval(ovalX, ovalY, diameter, diameter);

        // Имя вершины
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Dialog", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        g2d.drawString(name, x - textWidth / 2, y + 5);

        // Расстояние
        if (showDistances && distances != null && distances.containsKey(name)) {
            Integer dist = distances.get(name);
            String distStr = (dist == null || dist == 1000000000) ? "∞" : String.valueOf(dist);
            
            g2d.setFont(new Font("Dialog", Font.PLAIN, 11));
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(distStr);
            g2d.setColor(new Color(0, 0, 200));
            g2d.drawString("d=" + distStr, x - textWidth / 2, y + VERTEX_RADIUS + 18);
        }
    }

    public void updateSize() {
    if (vertexList != null && !vertexList.isEmpty()) {
        int maxX = 0;
        int maxY = 0;
        for (Vertex v : vertexList) {
            maxX = Math.max(maxX, v.getX() + VERTEX_RADIUS + 50);
            maxY = Math.max(maxY, v.getY() + VERTEX_RADIUS + 50);
        }
        setPreferredSize(new Dimension(Math.max(800, maxX), Math.max(600, maxY)));
        revalidate();
    }
}
}