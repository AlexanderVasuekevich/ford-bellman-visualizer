package visualizer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранит ориентированный взвешенный граф и стартовую вершину алгоритма.
 */
public class Graph {
    private final Map<String, Vertex> vertices = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private Vertex source;

    /**
     * Добавляет вершину без заданных координат.
     *
     * @param name уникальное имя вершины
     */
    public void addVertex(String name) {
        addVertex(name, 0, 0);
    }

    /**
     * Добавляет вершину с координатами для отображения.
     *
     * @param name уникальное имя вершины
     * @param x координата X на холсте
     * @param y координата Y на холсте
     */
    public void addVertex(String name, int x, int y) {
        validateVertexName(name);

        if (vertices.containsKey(name)) {
            throw new IllegalArgumentException("Вершина уже существует: " + name + ".");
        }

        vertices.put(name, new Vertex(name, x, y));
    }

    /**
     * Удаляет вершину и все связанные с ней ребра.
     *
     * @param name имя удаляемой вершины
     */
    public void removeVertex(String name) {
        Vertex vertex = getRequiredVertex(name);
        edges.removeIf(edge -> edge.getFrom() == vertex || edge.getTo() == vertex);

        if (source == vertex) {
            source = null;
        }

        vertices.remove(name);
    }

    /**
     * Переименовывает вершину и обновляет ключ в хранилище вершин.
     *
     * @param oldName текущее имя вершины
     * @param newName новое уникальное имя вершины
     */
    public void renameVertex(String oldName, String newName) {
        validateVertexName(newName);

        Vertex vertex = getRequiredVertex(oldName);
        if (vertices.containsKey(newName)) {
            throw new IllegalArgumentException("Вершина уже существует: " + newName + ".");
        }

        vertices.remove(oldName);
        vertex.rename(newName);
        vertices.put(newName, vertex);
    }

    /**
     * Добавляет ориентированное ребро между существующими вершинами.
     *
     * @param fromName имя начальной вершины
     * @param toName имя конечной вершины
     * @param weight вес ребра
     */
    public void addEdge(String fromName, String toName, int weight) {
        Vertex from = getRequiredVertex(fromName);
        Vertex to = getRequiredVertex(toName);
        edges.add(new Edge(from, to, weight));
    }

    /**
     * Удаляет указанное ребро, если оно есть в графе.
     *
     * @param edge удаляемое ребро
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    /**
     * Заменяет содержимое графа копией другого графа.
     *
     * @param other граф-источник
     */
    public void replaceWith(Graph other) {
        if (other == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }

        vertices.clear();
        edges.clear();
        source = null;

        for (Vertex vertex : other.getVertices()) {
            addVertex(vertex.getName(), vertex.getX(), vertex.getY());
        }

        if (other.getSource() != null) {
            setSource(other.getSource().getName());
        }

        for (Edge edge : other.getEdges()) {
            addEdge(edge.getFrom().getName(), edge.getTo().getName(), edge.getWeight());
        }
    }

    /**
     * Возвращает вершину по имени или {@code null}, если вершина не найдена.
     *
     * @param name имя вершины
     * @return найденная вершина или {@code null}
     */
    public Vertex getVertex(String name) {
        return vertices.get(name);
    }

    /**
     * Проверяет, объявлена ли вершина с таким именем.
     *
     * @param name имя вершины
     * @return {@code true}, если вершина есть в графе
     */
    public boolean hasVertex(String name) {
        return vertices.containsKey(name);
    }

    /**
     * Возвращает все вершины графа в порядке добавления.
     *
     * @return неизменяемая коллекция вершин
     */
    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    /**
     * Возвращает все ребра графа в порядке добавления.
     *
     * @return неизменяемый список ребер
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Возвращает все ребра, выходящие из указанной вершины.
     *
     * @param vertexName имя вершины
     * @return неизменяемый список исходящих ребер
     */
    public List<Edge> getOutgoingEdges(String vertexName) {
        Vertex vertex = getRequiredVertex(vertexName);
        List<Edge> result = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getFrom() == vertex) {
                result.add(edge);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Возвращает все ребра, связанные с указанной вершиной.
     *
     * @param vertexName имя вершины
     * @return неизменяемый список входящих и исходящих ребер
     */
    public List<Edge> getIncidentEdges(String vertexName) {
        Vertex vertex = getRequiredVertex(vertexName);
        List<Edge> result = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getFrom() == vertex || edge.getTo() == vertex) {
                result.add(edge);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Возвращает количество вершин.
     *
     * @return количество вершин в графе
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * Возвращает количество ребер.
     *
     * @return количество ребер в графе
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * Возвращает стартовую вершину алгоритма.
     *
     * @return стартовая вершина или {@code null}, если она не задана
     */
    public Vertex getSource() {
        return source;
    }

    /**
     * Устанавливает стартовую вершину по имени.
     *
     * @param name имя объявленной вершины
     */
    public void setSource(String name) {
        source = getRequiredVertex(name);
    }

    private Vertex getRequiredVertex(String name) {
        Vertex vertex = vertices.get(name);
        if (vertex == null) {
            throw new IllegalArgumentException("Вершина не существует: " + name
                    + ". Сначала создайте её кликом по свободному месту.");
        }
        return vertex;
    }

    private void validateVertexName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя вершины не может быть пустым.");
        }
    }
}
