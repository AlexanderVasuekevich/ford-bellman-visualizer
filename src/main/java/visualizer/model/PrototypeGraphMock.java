package visualizer.model;


public final class PrototypeGraphMock {
    private PrototypeGraphMock() {
    }

    public static Graph createGraph() {
        Graph graph = new Graph();

        graph.addVertex("A", 100, 120);
        graph.addVertex("B", 260, 80);
        graph.addVertex("C", 260, 200);
        graph.addVertex("D", 420, 120);
        graph.addVertex("E", 580, 160);

        graph.setSource("A");

        graph.addEdge("A", "B", 4);
        graph.addEdge("A", "C", 5);
        graph.addEdge("B", "C", -2);
        graph.addEdge("B", "D", 6);
        graph.addEdge("C", "D", 3);
        graph.addEdge("C", "E", 4);
        graph.addEdge("D", "E", -1);

        return graph;
    }
}
