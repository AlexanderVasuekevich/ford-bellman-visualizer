package visualizer.ui;

import visualizer.model.Edge;
import visualizer.model.Graph;
import visualizer.model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private boolean editMode;
    private Runnable graphChangedHandler;
    private Point panOrigin; // Точка начала перетаскивания средней кнопкой

    public GraphPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        this.graph = null;
        this.vertexList = new ArrayList<>();
        this.edgeList = new ArrayList<>();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleEditClick(e);
            }
        });
        installPanHandler();
    }

    /**
     * Перетаскивание графа средней кнопкой мыши (Стрижков, Final).
     * Пока средняя кнопка зажата, все вершины смещаются вслед за курсором.
     */
    private void installPanHandler() {
        MouseAdapter panHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    panOrigin = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    panOrigin = null;
                    updateCursor();
                    updateSize();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (panOrigin != null) {
                    translateGraph(e.getX() - panOrigin.x, e.getY() - panOrigin.y);
                    panOrigin = e.getPoint();
                }
            }
        };
        addMouseListener(panHandler);
        addMouseMotionListener(panHandler);
    }

    /**
     * Смещает все вершины графа на (dx, dy), не давая графу уйти
     * за левый/верхний край холста.
     */
    private void translateGraph(int dx, int dy) {
        if (vertexList == null || vertexList.isEmpty()) {
            return;
        }

        int margin = VERTEX_RADIUS + 10;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (Vertex v : vertexList) {
            minX = Math.min(minX, v.getX() + dx);
            minY = Math.min(minY, v.getY() + dy);
        }
        if (minX < margin) {
            dx += margin - minX;
        }
        if (minY < margin) {
            dy += margin - minY;
        }
        if (dx == 0 && dy == 0) {
            return;
        }

        for (Vertex v : vertexList) {
            v.setX(v.getX() + dx);
            v.setY(v.getY() + dy);
        }
        repaint();
    }

    private void updateCursor() {
        setCursor(editMode
                ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                : Cursor.getDefaultCursor());
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
            updateSize();
        }
        repaint();
    }

    public Graph getGraph() {
        return graph;
    }

    public void refreshGraph() {
        if (graph != null) {
            this.vertexList = new ArrayList<>(graph.getVertices());
            this.edgeList = graph.getEdges();
            updateSize();
        }
        repaint();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        updateCursor();
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setGraphChangedHandler(Runnable graphChangedHandler) {
        this.graphChangedHandler = graphChangedHandler;
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

    /**
     * Полный сброс данных алгоритма: подсветка, расстояния, предшественники.
     * Нужен после редактирования графа, чтобы на холсте не оставались
     * устаревшие метки «d=...» от предыдущего запуска.
     */
    public void clearAlgorithmData() {
        this.currentProcessingEdgeIndex = -1;
        this.updatedVertexName = null;
        this.distances = null;
        this.predecessors = null;
        this.showDistances = false;
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

            boolean isSource = graph != null && graph.getSource() == v;
            drawVertex(g2d, v, isFrom, isTo, isUpdated, isSource);
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

        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Для встречных рёбер (A->B и B->A) смещаем линию и подпись в сторону,
        // чтобы они не накладывались. Обычные одиночные рёбра остаются прямыми.
        double perpX = -Math.sin(angle);
        double perpY = Math.cos(angle);
        double lineOffset = hasReverseEdge(edge) ? 14.0 : 0.0;

        double ox1 = x1 + perpX * lineOffset;
        double oy1 = y1 + perpY * lineOffset;
        double ox2 = x2 + perpX * lineOffset;
        double oy2 = y2 + perpY * lineOffset;

        int startX = (int) (ox1 + VERTEX_RADIUS * Math.cos(angle));
        int startY = (int) (oy1 + VERTEX_RADIUS * Math.sin(angle));
        int endX = (int) (ox2 - VERTEX_RADIUS * Math.cos(angle));
        int endY = (int) (oy2 - VERTEX_RADIUS * Math.sin(angle));

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

        // Подпись веса находится рядом со своей линией. У встречных рёбер
        // направления перпендикуляра противоположные, поэтому подписи расходятся.
        double labelOffset = lineOffset + 12.0;
        int textX = (int) ((x1 + x2) / 2.0 + perpX * labelOffset);
        int textY = (int) ((y1 + y2) / 2.0 + perpY * labelOffset);

        g2d.setFont(new Font("Dialog", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String weightStr = String.valueOf(weight);
        int textWidth = fm.stringWidth(weightStr);
        int textHeight = fm.getAscent();

        int bgWidth = Math.max(24, textWidth + 10);
        int bgHeight = 20;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(textX - bgWidth / 2, textY - bgHeight / 2, bgWidth, bgHeight);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawOval(textX - bgWidth / 2, textY - bgHeight / 2, bgWidth, bgHeight);
        g2d.drawString(weightStr, textX - textWidth / 2, textY + textHeight / 2 - 2);
    }

    /** Проверяет, есть ли в графе встречное ребро (to -> from) для данного. */
    private boolean hasReverseEdge(Edge edge) {
        for (Edge other : edgeList) {
            if (other.getFrom().equals(edge.getTo()) && other.getTo().equals(edge.getFrom())) {
                return true;
            }
        }
        return false;
    }


    private void drawVertex(Graphics2D g2d, Vertex vertex, boolean isFrom, boolean isTo,
                            boolean isUpdated, boolean isSource) {
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
        } else if (isSource) {
            // Стартовая вершина выделяется всегда, в том числе сразу после
            // назначения командой SOURCE в режиме редактирования
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

        // Подпись стартовой вершины
        if (isSource) {
            g2d.setFont(new Font("Dialog", Font.PLAIN, 11));
            g2d.setColor(new Color(0, 130, 0));
            String sourceMark = "старт";
            int markWidth = g2d.getFontMetrics().stringWidth(sourceMark);
            g2d.drawString(sourceMark, x - markWidth / 2, y - VERTEX_RADIUS - 6);
        }

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

    private void handleEditClick(MouseEvent e) {
        if (!editMode || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        if (graph == null) {
            setGraph(new Graph());
        }

        Vertex clickedVertex = findVertexAt(e.getX(), e.getY());
        Window parent = SwingUtilities.getWindowAncestor(this);
        VertexDialog dialog = new VertexDialog(parent, graph, clickedVertex, e.getX(), e.getY(), () -> {
            refreshGraph();
            if (graphChangedHandler != null) {
                graphChangedHandler.run();
            }
        });
        dialog.setVisible(true);
    }

    private Vertex findVertexAt(int x, int y) {
        if (vertexList == null) {
            return null;
        }
        for (Vertex vertex : vertexList) {
            int dx = x - vertex.getX();
            int dy = y - vertex.getY();
            if (dx * dx + dy * dy <= VERTEX_RADIUS * VERTEX_RADIUS) {
                return vertex;
            }
        }
        return null;
    }
}
