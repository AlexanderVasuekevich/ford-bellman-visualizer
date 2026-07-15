package visualizer.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения информации о шаге алгоритма Форда-Беллмана
 */
public class AlgorithmStep {
    private final int passNumber;          // Номер прохода
    private final int edgeIndex;           // Индекс обрабатываемого ребра
    private final String fromVertex;       // Откуда
    private final String toVertex;         // Куда
    private final int weight;              // Вес ребра
    private final String explanation;      // Пояснение шага
    private final Map<String, Integer> distances;    // Текущие расстояния
    private final Map<String, String> predecessors;  // Текущие предшественники
    private final boolean relaxed;         // Была ли релаксация

    public AlgorithmStep(int passNumber, int edgeIndex, String fromVertex, String toVertex, 
                        int weight, String explanation, Map<String, Integer> distances, 
                        Map<String, String> predecessors) {
        this(passNumber, edgeIndex, fromVertex, toVertex, weight, explanation, distances, predecessors, false);
    }

    public AlgorithmStep(int passNumber, int edgeIndex, String fromVertex, String toVertex, 
                        int weight, String explanation, Map<String, Integer> distances, 
                        Map<String, String> predecessors, boolean relaxed) {
        this.passNumber = passNumber;
        this.edgeIndex = edgeIndex;
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
        this.weight = weight;
        this.explanation = explanation;
        this.distances = new HashMap<>(distances);
        this.predecessors = new HashMap<>(predecessors);
        this.relaxed = relaxed;
    }

    public int getPassNumber() { 
        return passNumber; 
    }
    
    public int getEdgeIndex() { 
        return edgeIndex; 
    }
    
    public String getFromVertex() { 
        return fromVertex; 
    }
    
    public String getToVertex() { 
        return toVertex; 
    }
    
    public int getWeight() { 
        return weight; 
    }
    
    public String getExplanation() { 
        return explanation; 
    }
    
    public Map<String, Integer> getDistances() { 
        return new HashMap<>(distances); 
    }
    
    public Map<String, String> getPredecessors() { 
        return new HashMap<>(predecessors); 
    }
    
    public boolean isRelaxed() { 
        return relaxed; 
    }

    @Override
    public String toString() {
        return String.format("Проход %d, ребро %d: %s -> %s (вес: %d) %s",
            passNumber, edgeIndex, fromVertex, toVertex, weight, 
            relaxed ? "РЕЛАКСАЦИЯ" : "");
    }
}