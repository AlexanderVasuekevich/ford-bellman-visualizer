package visualizer.model;

/**
 * Ориентированное взвешенное ребро графа.
 */
public class Edge {
    private final Vertex from;
    private final Vertex to;
    private final int weight;

    Edge(Vertex from, Vertex to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    /**
     * Возвращает начальную вершину ребра.
     *
     * @return начальная вершина
     */
    public Vertex getFrom() {
        return from;
    }

    /**
     * Возвращает конечную вершину ребра.
     *
     * @return конечная вершина
     */
    public Vertex getTo() {
        return to;
    }

    /**
     * Возвращает вес ребра.
     *
     * @return вес ребра
     */
    public int getWeight() {
        return weight;
    }
}
