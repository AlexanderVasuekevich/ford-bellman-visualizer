package visualizer.algorithm;

import visualizer.model.Edge;
import visualizer.model.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация алгоритма Форда-Беллмана с логированием шагов
 */
public class BellmanFord {
    private Map<String, Integer> dist = new HashMap<>();
    private Map<String, String> predecessors = new HashMap<>();
    private List<AlgorithmStep> steps = new ArrayList<>();
    private final int INF = 1000000000;

    /**
     * Запуск алгоритма Форда-Беллмана
     * @param vertices список вершин
     * @param edges список ребер
     * @param startVertex начальная вершина
     * @return список шагов алгоритма для визуализации
     */
    public List<AlgorithmStep> run(List<Vertex> vertices, List<Edge> edges, Vertex startVertex) {
        steps.clear();
        
        for (Vertex vertex : vertices) {
            dist.put(vertex.getName(), INF);
            predecessors.put(vertex.getName(), null);
        }
        dist.put(startVertex.getName(), 0);
        
        // Добавляем начальный шаг
        steps.add(new AlgorithmStep(0, -1, null, null, 0, 
            "Инициализация: расстояние до стартовой вершины '" + startVertex.getName() + "' = 0, остальные = ∞",
            new HashMap<>(dist), new HashMap<>(predecessors)));

        int v = vertices.size();
        
        for (int i = 1; i < v; i++) {
            boolean anyRelaxed = false;
            
            for (int edgeIndex = 0; edgeIndex < edges.size(); edgeIndex++) {
                Edge edge = edges.get(edgeIndex);
                String fromName = edge.getFrom().getName();
                String toName = edge.getTo().getName();
                int weight = edge.getWeight();
                
                // Проверка условия релаксации
                boolean relaxed = false;
                String explanation = "";
                
                Integer distFrom = dist.get(fromName);
                Integer distTo = dist.get(toName);
                
                if (distFrom != INF) {
                    int newDist = distFrom + weight;
                    if (newDist < distTo) {
                        dist.put(toName, newDist);
                        predecessors.put(toName, fromName);
                        relaxed = true;
                        anyRelaxed = true;
                        explanation = String.format(
                            "Релаксация: d[%s] (%d) + w(%d) = %d < d[%s] (%d) → обновляем d[%s] = %d",
                            fromName, distFrom, weight, newDist, toName, distTo, toName, newDist
                        );
                    } else {
                        explanation = String.format(
                            "Проверка: d[%s] (%d) + w(%d) = %d ≥ d[%s] (%d) → без изменений",
                            fromName, distFrom, weight, newDist, toName, distTo
                        );
                    }
                } else {
                    explanation = String.format(
                        "Пропуск: d[%s] = ∞, ребро (%s → %s) не рассматривается",
                        fromName, fromName, toName
                    );
                }
                
                // Сохрание шага
                steps.add(new AlgorithmStep(
                    i, 
                    edgeIndex, 
                    fromName, 
                    toName, 
                    weight,
                    explanation,
                    new HashMap<>(dist), 
                    new HashMap<>(predecessors),
                    relaxed
                ));
            }
            
            // Если ни одно расстояние не было обновлено - досрочный выход
            if (!anyRelaxed) {
                steps.add(new AlgorithmStep(
                    i, -1, null, null, 0,
                    "Проход " + i + ": ни одно расстояние не было обновлено → алгоритм завершен досрочно",
                    new HashMap<>(dist), new HashMap<>(predecessors)
                ));
                break;
            }
        }

        // Проверка на отрицательные циклы
        for (Edge edge : edges) {
            String fromName = edge.getFrom().getName();
            String toName = edge.getTo().getName();
            int weight = edge.getWeight();
            
            Integer distFrom = dist.get(fromName);
            Integer distTo = dist.get(toName);
            
            if (distFrom != INF && distFrom + weight < distTo) {
                throw new IllegalArgumentException("В графе обнаружен цикл отрицательного веса!");
            }
        }
        
        steps.add(new AlgorithmStep(
            vertices.size(), -1, null, null, 0,
            "Алгоритм завершен. Найдены кратчайшие расстояния от вершины '" + startVertex.getName() + "'",
            new HashMap<>(dist), new HashMap<>(predecessors)
        ));
        
        return steps;
    }

    public Map<String, Integer> getDist() {
        return dist;
    }

    public Map<String, String> getPredecessors() {
        return predecessors;
    }
}