package visualizer.algorithm;

import visualizer.model.Graph;

import java.util.List;

/**
 * Контроллер истории шагов алгоритма для навигации вперед и назад.
 */
public class StepHistory {
    private final List<StepState> steps;
    private int currentIndex;

    private StepHistory(List<StepState> steps) {
        this.steps = List.copyOf(steps);
        this.currentIndex = this.steps.isEmpty() ? -1 : 0;
    }

    /**
     * Строит историю шагов алгоритма для указанного графа.
     *
     * @param graph граф со стартовой вершиной
     * @return контроллер истории шагов
     */
    public static StepHistory fromGraph(Graph graph) {
        return new StepHistory(BellmanFord.buildStepHistory(graph));
    }

    /**
     * Создает контроллер для уже построенного списка шагов.
     *
     * @param steps шаги алгоритма
     * @return контроллер истории шагов
     */
    public static StepHistory fromSteps(List<StepState> steps) {
        return new StepHistory(steps);
    }

    /**
     * Возвращает текущий шаг.
     *
     * @return текущий шаг или {@code null}, если история пустая
     */
    public StepState current() {
        if (currentIndex < 0) {
            return null;
        }
        return steps.get(currentIndex);
    }

    /**
     * Переходит к следующему шагу, если он есть.
     *
     * @return текущий шаг после попытки перехода
     */
    public StepState next() {
        if (hasNext()) {
            currentIndex++;
        }
        return current();
    }

    /**
     * Переходит к предыдущему шагу, если он есть.
     *
     * @return текущий шаг после попытки перехода
     */
    public StepState previous() {
        if (hasPrevious()) {
            currentIndex--;
        }
        return current();
    }

    /**
     * Проверяет, есть ли следующий шаг.
     *
     * @return {@code true}, если можно перейти вперед
     */
    public boolean hasNext() {
        return currentIndex >= 0 && currentIndex < steps.size() - 1;
    }

    /**
     * Проверяет, есть ли предыдущий шаг.
     *
     * @return {@code true}, если можно перейти назад
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * Возвращает текущий индекс шага.
     *
     * @return индекс текущего шага или -1, если история пустая
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Возвращает количество шагов в истории.
     *
     * @return количество шагов
     */
    public int size() {
        return steps.size();
    }

    /**
     * Проверяет, пуста ли история.
     *
     * @return {@code true}, если шагов нет
     */
    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /**
     * Возвращает неизменяемый список шагов.
     *
     * @return список шагов
     */
    public List<StepState> asList() {
        return steps;
    }
}
