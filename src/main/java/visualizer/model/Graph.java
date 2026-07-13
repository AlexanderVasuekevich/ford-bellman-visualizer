package visualizer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<String, Vertex> vertices = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private Vertex source;

    public void addVertex(String name, int x, int y) {
        validateVertexName(name);

        if (vertices.containsKey(name)) {
            throw new IllegalArgumentException("Vertex already exists: " + name);
        }

        vertices.put(name, new Vertex(name, x, y));
    }

    public void removeVertex(String name) {
        Vertex vertex = getRequiredVertex(name);
        edges.removeIf(edge -> edge.getFrom() == vertex || edge.getTo() == vertex);

        if (source == vertex) {
            source = null;
        }

        vertices.remove(name);
    }

    public void renameVertex(String oldName, String newName) {
        validateVertexName(newName);

        Vertex vertex = getRequiredVertex(oldName);
        if (vertices.containsKey(newName)) {
            throw new IllegalArgumentException("Vertex already exists: " + newName);
        }

        vertices.remove(oldName);
        vertex.rename(newName);
        vertices.put(newName, vertex);
    }

    public void addEdge(String fromName, String toName, int weight) {
        Vertex from = getRequiredVertex(fromName);
        Vertex to = getRequiredVertex(toName);
        edges.add(new Edge(from, to, weight));
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    public Vertex getVertex(String name) {
        return vertices.get(name);
    }

    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public Vertex getSource() {
        return source;
    }

    public void setSource(String name) {
        source = getRequiredVertex(name);
    }

    private Vertex getRequiredVertex(String name) {
        Vertex vertex = vertices.get(name);
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex does not exist: " + name);
        }
        return vertex;
    }

    private void validateVertexName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vertex name must not be blank");
        }
    }
}
