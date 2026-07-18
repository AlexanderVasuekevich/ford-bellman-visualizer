package visualizer.algorithm;

import visualizer.model.Edge;
import visualizer.model.Graph;
import visualizer.model.Vertex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация алгоритма Форда-Беллмана с генерацией истории шагов.
 */
public final class BellmanFord {
    public static final int INF = 1000000000;

    private BellmanFord() {
    }

    /**
     * Формирует полную историю шагов алгоритма для пошаговой визуализации.
     *
     * @param graph граф со стартовой вершиной
     * @return история состояний алгоритма
     */
    public static List<StepState> buildStepHistory(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }
        if (graph.getSource() == null) {
            throw new IllegalArgumentException("Graph source must be set");
        }

        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        List<Edge> edges = graph.getEdges();
        Map<String, Integer> distances = new LinkedHashMap<>();
        Map<String, String> predecessors = new LinkedHashMap<>();
        List<StepState> history = new ArrayList<>();

        for (Vertex vertex : vertices) {
            distances.put(vertex.getName(), INF);
            predecessors.put(vertex.getName(), null);
        }
        distances.put(graph.getSource().getName(), 0);

        int stepNumber = 0;
        int maxPass = Math.max(vertices.size() - 1, 0);
        history.add(new StepState(
                stepNumber,
                0,
                null,
                distances,
                predecessors,
                StepExplanationBuilder.initial(graph.getSource().getName()),
                false,
                false,
                null
        ));

        for (int passNumber = 1; passNumber <= maxPass; passNumber++) {
            for (Edge edge : edges) {
                stepNumber++;
                RelaxationResult result = relax(edge, distances, predecessors);
                history.add(new StepState(
                        stepNumber,
                        passNumber,
                        edge,
                        distances,
                        predecessors,
                        StepExplanationBuilder.relaxation(stepNumber, passNumber, maxPass, edge, result),
                        result.updated(),
                        false,
                        result.updated() ? edge.getTo().getName() : null
                ));
            }
        }

        // Дополнительный проход (V-й). После V-1 проходов расстояния должны
        // стабилизироваться. Если на этом проходе какое-то расстояние всё ещё
        // уменьшается — это и есть обнаружение отрицательного цикла. Здесь мы
        // РЕАЛЬНО выполняем релаксации, чтобы пользователь видел сами итерации
        // (как расстояния продолжают падать), а не только итоговое сообщение.
        int negativeCyclePass = vertices.size();
        boolean cycleFound = false;
        Set<String> affectedVertices = new LinkedHashSet<>();
        Set<String> updatedOnExtraPass = new LinkedHashSet<>();
        for (Edge edge : edges) {
            stepNumber++;
            RelaxationResult result = relax(edge, distances, predecessors);
            if (result.updated()) {
                cycleFound = true;
                updatedOnExtraPass.add(edge.getTo().getName());
                affectedVertices.addAll(findNegativeCycleAffectedVertices(edge.getTo().getName(), edges));
            }

            history.add(new StepState(
                    stepNumber,
                    negativeCyclePass,
                    edge,
                    distances,
                    predecessors,
                    StepExplanationBuilder.negativeCycleIteration(stepNumber, negativeCyclePass, edge, result),
                    result.updated(),
                    cycleFound,
                    result.updated() ? edge.getTo().getName() : null,
                    cycleFound ? Set.copyOf(affectedVertices) : Set.of()
            ));
        }

        if (cycleFound) {
            Set<String> cycleVertices =
                    findNegativeCycleVertices(updatedOnExtraPass, predecessors, vertices.size());
            stepNumber++;
            history.add(new StepState(
                    stepNumber,
                    negativeCyclePass,
                    null,
                    distances,
                    predecessors,
                    StepExplanationBuilder.negativeCycleConclusion(affectedVertices),
                    false,
                    true,
                    null,
                    Set.copyOf(affectedVertices),
                    cycleVertices
            ));
            return history;
        }

        stepNumber++;
        history.add(new StepState(
                stepNumber,
                maxPass,
                null,
                distances,
                predecessors,
                StepExplanationBuilder.finished(graph.getSource().getName(), distances, predecessors),
                false,
                false,
                null
        ));

        return history;
    }

    /**
     * Находит вершины, лежащие непосредственно на отрицательных циклах.
     *
     * Для каждой вершины, расстояние до которой уменьшилось на дополнительном
     * проходе, выполняется V шагов назад по предшественникам — так мы
     * гарантированно попадаем внутрь цикла в графе предшественников, после
     * чего цикл обходится целиком.
     *
     * @param updatedVertices вершины, обновлённые на дополнительном проходе
     * @param predecessors текущие предшественники вершин
     * @param vertexCount количество вершин в графе
     * @return множество имен вершин, лежащих на отрицательных циклах
     */
    private static Set<String> findNegativeCycleVertices(
            Set<String> updatedVertices,
            Map<String, String> predecessors,
            int vertexCount
    ) {
        Set<String> cycleVertices = new LinkedHashSet<>();
        for (String updated : updatedVertices) {
            String walker = updated;
            for (int i = 0; i < vertexCount && walker != null; i++) {
                walker = predecessors.get(walker);
            }
            if (walker == null) {
                continue;
            }

            String current = walker;
            do {
                if (!cycleVertices.add(current)) {
                    break;
                }
                current = predecessors.get(current);
            } while (current != null && !current.equals(walker));
        }
        return cycleVertices;
    }

    private static RelaxationResult relax(
            Edge edge,
            Map<String, Integer> distances,
            Map<String, String> predecessors
    ) {
        String fromName = edge.getFrom().getName();
        String toName = edge.getTo().getName();
        int fromDistance = distances.get(fromName);
        int toDistance = distances.get(toName);

        if (fromDistance == INF) {
            return new RelaxationResult(false, fromDistance, toDistance, null);
        }

        int candidate = fromDistance + edge.getWeight();
        if (candidate < toDistance) {
            distances.put(toName, candidate);
            predecessors.put(toName, fromName);
            return new RelaxationResult(true, fromDistance, toDistance, candidate);
        }

        return new RelaxationResult(false, fromDistance, toDistance, candidate);
    }

    private static Set<String> findNegativeCycleAffectedVertices(String startVertexName, List<Edge> edges) {
        Set<String> affected = new LinkedHashSet<>();
        collectReachableVertices(startVertexName, edges, affected);
        return affected;
    }

    private static void collectReachableVertices(String vertexName, List<Edge> edges, Set<String> affected) {
        if (!affected.add(vertexName)) {
            return;
        }

        for (Edge edge : edges) {
            if (edge.getFrom().getName().equals(vertexName)) {
                collectReachableVertices(edge.getTo().getName(), edges, affected);
            }
        }
    }

    record RelaxationResult(
            boolean updated,
            int fromDistance,
            int oldToDistance,
            Integer candidateDistance
    ) {
    }
}
