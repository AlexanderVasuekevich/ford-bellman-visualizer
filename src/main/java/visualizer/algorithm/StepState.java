package visualizer.algorithm;

import visualizer.model.Edge;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Состояние одного шага алгоритма Форда-Беллмана.
 */
public class StepState {
    private final int stepNumber;
    private final int passNumber;
    private final Edge currentEdge;
    private final Map<String, Integer> distances;
    private final Map<String, String> predecessors;
    private final String explanation;
    private final boolean updated;
    private final boolean negativeCycle;
    private final String updatedVertexName;
    private final Set<String> negativeCycleAffectedVertices;

    /**
     * Создает снимок состояния алгоритма.
     *
     * @param stepNumber общий номер шага
     * @param passNumber номер прохода алгоритма
     * @param currentEdge текущее рассматриваемое ребро
     * @param distances текущие расстояния до вершин
     * @param predecessors текущие предшественники вершин
     * @param explanation текстовое пояснение шага
     * @param updated было ли обновлено расстояние
     * @param negativeCycle обнаружен ли отрицательный цикл
     * @param updatedVertexName имя вершины, расстояние до которой обновилось
     */
    public StepState(
            int stepNumber,
            int passNumber,
            Edge currentEdge,
            Map<String, Integer> distances,
            Map<String, String> predecessors,
            String explanation,
            boolean updated,
            boolean negativeCycle,
            String updatedVertexName
    ) {
        this(
                stepNumber,
                passNumber,
                currentEdge,
                distances,
                predecessors,
                explanation,
                updated,
                negativeCycle,
                updatedVertexName,
                Set.of()
        );
    }

    /**
     * Создает снимок состояния алгоритма с информацией о вершинах,
     * затронутых отрицательным циклом.
     *
     * @param stepNumber общий номер шага
     * @param passNumber номер прохода алгоритма
     * @param currentEdge текущее рассматриваемое ребро
     * @param distances текущие расстояния до вершин
     * @param predecessors текущие предшественники вершин
     * @param explanation текстовое пояснение шага
     * @param updated было ли обновлено расстояние
     * @param negativeCycle обнаружен ли отрицательный цикл
     * @param updatedVertexName имя вершины, расстояние до которой обновилось
     * @param negativeCycleAffectedVertices вершины с неопределенным расстоянием
     */
    public StepState(
            int stepNumber,
            int passNumber,
            Edge currentEdge,
            Map<String, Integer> distances,
            Map<String, String> predecessors,
            String explanation,
            boolean updated,
            boolean negativeCycle,
            String updatedVertexName,
            Set<String> negativeCycleAffectedVertices
    ) {
        this.stepNumber = stepNumber;
        this.passNumber = passNumber;
        this.currentEdge = currentEdge;
        this.distances = new LinkedHashMap<>(distances);
        this.predecessors = new LinkedHashMap<>(predecessors);
        this.explanation = explanation;
        this.updated = updated;
        this.negativeCycle = negativeCycle;
        this.updatedVertexName = updatedVertexName;
        this.negativeCycleAffectedVertices = new LinkedHashSet<>(negativeCycleAffectedVertices);
    }

    /**
     * Возвращает общий номер шага.
     *
     * @return номер шага
     */
    public int getStepNumber() {
        return stepNumber;
    }

    /**
     * Возвращает номер прохода алгоритма.
     *
     * @return номер прохода
     */
    public int getPassNumber() {
        return passNumber;
    }

    /**
     * Возвращает текущее рассматриваемое ребро.
     *
     * @return текущее ребро или {@code null}, если шаг не связан с ребром
     */
    public Edge getCurrentEdge() {
        return currentEdge;
    }

    /**
     * Возвращает текущие расстояния до вершин.
     *
     * @return копия расстояний
     */
    public Map<String, Integer> getDistances() {
        return new LinkedHashMap<>(distances);
    }

    /**
     * Возвращает текущих предшественников вершин.
     *
     * @return копия предшественников
     */
    public Map<String, String> getPredecessors() {
        return new LinkedHashMap<>(predecessors);
    }

    /**
     * Возвращает текстовое пояснение шага.
     *
     * @return пояснение
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Проверяет, обновилось ли расстояние на этом шаге.
     *
     * @return {@code true}, если релаксация изменила расстояние
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * Проверяет, обнаружен ли отрицательный цикл на этом шаге.
     *
     * @return {@code true}, если обнаружен отрицательный цикл
     */
    public boolean hasNegativeCycle() {
        return negativeCycle;
    }

    /**
     * Возвращает имя обновленной вершины.
     *
     * @return имя вершины или {@code null}, если обновления не было
     */
    public String getUpdatedVertexName() {
        return updatedVertexName;
    }

    /**
     * Возвращает вершины, расстояния до которых неопределены из-за отрицательного цикла.
     *
     * @return копия множества имен вершин
     */
    public Set<String> getNegativeCycleAffectedVertices() {
        return new LinkedHashSet<>(negativeCycleAffectedVertices);
    }
}
