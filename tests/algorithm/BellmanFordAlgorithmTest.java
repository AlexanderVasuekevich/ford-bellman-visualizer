package algorithm;

import visualizer.algorithm.BellmanFord;
import visualizer.algorithm.StepHistory;
import visualizer.algorithm.StepState;
import visualizer.export.AlgorithmResultExporter;
import visualizer.model.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Комплексное тестирование алгоритма Форда-Беллмана (Стрижков, Final):
 * отрицательные циклы, недостижимые вершины, большие графы (10+ вершин),
 * навигация по истории шагов, экспорт результата.
 *
 * Запуск из корня репозитория:
 * javac -d /tmp/ford-bellman-test-classes $(find src/main/java tests -name '*.java')
 * java -cp /tmp/ford-bellman-test-classes algorithm.BellmanFordAlgorithmTest
 */
public final class BellmanFordAlgorithmTest {

    private BellmanFordAlgorithmTest() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Комплексное тестирование алгоритма Форда-Беллмана");
        System.out.println();
        testSimpleGraph();
        testNegativeCycleGraph();
        testDisconnectedGraph();
        testBigGraph();
        testHistoryNavigation();
        testExportSuccess();
        testExportNegativeCycle();
        testGraphEditingScenario();
        System.out.println();
        System.out.println("Все тесты алгоритма пройдены.");
    }

    // Граф из примера спецификации: A->B 4, A->C 5, B->C -2, C->D 3.
    private static Graph simpleGraph() {
        Graph graph = new Graph();
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.setSource("A");
        graph.addEdge("A", "B", 4);
        graph.addEdge("A", "C", 5);
        graph.addEdge("B", "C", -2);
        graph.addEdge("C", "D", 3);
        return graph;
    }

    // Граф из раздела спецификации про выходной файл: цикл C->D->E->C (вес -2),
    // F достижима из цикла, G недостижима из источника.
    private static Graph negativeCycleGraph() {
        Graph graph = new Graph();
        for (String name : List.of("A", "B", "C", "D", "E", "F", "G")) {
            graph.addVertex(name);
        }
        graph.setSource("A");
        graph.addEdge("A", "B", 3);
        graph.addEdge("A", "C", 1);
        graph.addEdge("C", "D", 2);
        graph.addEdge("D", "E", -5);
        graph.addEdge("E", "C", 1);
        graph.addEdge("E", "F", 4);
        return graph;
    }

    private static void testSimpleGraph() {
        StepHistory history = StepHistory.fromGraph(simpleGraph());
        StepState finalStep = history.asList().get(history.size() - 1);
        Map<String, Integer> dist = finalStep.getDistances();
        Map<String, String> pred = finalStep.getPredecessors();

        assertIntEquals(18, history.size(), "размер истории простого графа");
        assertTrue(!finalStep.hasNegativeCycle(), "простой граф: цикла быть не должно");
        assertIntEquals(0, dist.get("A"), "dist[A]");
        assertIntEquals(4, dist.get("B"), "dist[B]");
        assertIntEquals(2, dist.get("C"), "dist[C]");
        assertIntEquals(5, dist.get("D"), "dist[D]");
        assertObjectEquals("B", pred.get("C"), "pred[C]");
        assertObjectEquals("C", pred.get("D"), "pred[D]");
        assertTrue(finalStep.getExplanation().contains("путь: A -> B -> C -> D"),
                "финальное пояснение должно содержать восстановленный путь до D");
        printOk("Простой граф: расстояния, предшественники, пути");
    }

    private static void testNegativeCycleGraph() {
        StepHistory history = StepHistory.fromGraph(negativeCycleGraph());
        StepState finalStep = history.asList().get(history.size() - 1);

        assertIntEquals(44, history.size(), "размер истории графа с циклом");
        assertTrue(finalStep.hasNegativeCycle(), "отрицательный цикл должен быть обнаружен");
        assertObjectEquals(Set.of("C", "D", "E", "F"), finalStep.getNegativeCycleAffectedVertices(),
                "вершины, достижимые из цикла");
        assertObjectEquals(Set.of("C", "D", "E"), finalStep.getNegativeCycleVertices(),
                "вершины самого цикла");
        assertIntEquals(3, finalStep.getDistances().get("B"), "dist[B] не затронут циклом");
        assertIntEquals(BellmanFord.INF, finalStep.getDistances().get("G"), "G недостижима");
        printOk("Отрицательный цикл: детекция, вершины цикла, затронутые вершины");
    }

    private static void testDisconnectedGraph() {
        Graph graph = new Graph();
        for (String name : List.of("A", "B", "C", "D", "E")) {
            graph.addVertex(name);
        }
        graph.setSource("A");
        graph.addEdge("A", "B", 2);
        graph.addEdge("B", "C", 3);
        graph.addEdge("D", "E", 1);

        StepHistory history = StepHistory.fromGraph(graph);
        StepState finalStep = history.asList().get(history.size() - 1);

        assertTrue(!finalStep.hasNegativeCycle(), "несвязный граф: цикла нет");
        assertIntEquals(5, finalStep.getDistances().get("C"), "dist[C]");
        assertIntEquals(BellmanFord.INF, finalStep.getDistances().get("D"), "D недостижима");
        assertIntEquals(BellmanFord.INF, finalStep.getDistances().get("E"), "E недостижима");
        assertTrue(finalStep.getExplanation().contains("D: недостижима (INF)"),
                "пояснение должно отмечать недостижимую вершину");
        printOk("Несвязный граф: недостижимые вершины помечены INF");
    }

    private static void testBigGraph() {
        // Цепочка из 12 вершин: V1 -> V2 -> ... -> V12 с весами 1..11.
        Graph graph = new Graph();
        for (int i = 1; i <= 12; i++) {
            graph.addVertex("V" + i);
        }
        graph.setSource("V1");
        for (int i = 1; i <= 11; i++) {
            graph.addEdge("V" + i, "V" + (i + 1), i);
        }

        StepHistory history = StepHistory.fromGraph(graph);
        StepState finalStep = history.asList().get(history.size() - 1);

        assertIntEquals(134, history.size(), "размер истории большого графа");
        assertIntEquals(66, finalStep.getDistances().get("V12"), "dist[V12] = 1+2+...+11");
        assertIntEquals(28, finalStep.getDistances().get("V8"), "dist[V8]");
        printOk("Большой граф (12 вершин): расстояния корректны");
    }

    private static void testHistoryNavigation() {
        StepHistory history = StepHistory.fromGraph(simpleGraph());

        assertTrue(!history.hasPrevious(), "в начале нет шага назад");
        int forwardSteps = 0;
        while (history.hasNext()) {
            history.next();
            forwardSteps++;
        }
        assertIntEquals(history.size() - 1, forwardSteps, "число переходов вперед");
        assertTrue(!history.hasNext(), "в конце нет шага вперед");

        while (history.hasPrevious()) {
            history.previous();
        }
        assertIntEquals(0, history.getCurrentIndex(), "возврат к начальному шагу");
        assertIntEquals(0, history.current().getStepNumber(), "начальный шаг — инициализация");
        printOk("История шагов: навигация вперед/назад согласована");
    }

    private static void testExportSuccess() throws Exception {
        Graph graph = simpleGraph();
        StepHistory history = StepHistory.fromGraph(graph);
        Path file = Files.createTempDirectory("fb-test").resolve("result.txt");

        AlgorithmResultExporter.save(graph, history, file);
        List<String> lines = Files.readAllLines(file);

        assertObjectEquals("Алгоритм: Форда-Беллмана", lines.get(0), "заголовок");
        assertObjectEquals("Источник: A", lines.get(1), "источник");
        assertObjectEquals("Статус: успешно завершено", lines.get(2), "статус");
        assertTrue(lines.contains("D; 5; C; A -> B -> C -> D"), "строка вершины D с путем");
        printOk("Экспорт: успешный результат с восстановленными путями");
    }

    private static void testExportNegativeCycle() throws Exception {
        Graph graph = negativeCycleGraph();
        StepHistory history = StepHistory.fromGraph(graph);
        Path file = Files.createTempDirectory("fb-test").resolve("result-cycle.txt");

        AlgorithmResultExporter.save(graph, history, file);
        List<String> lines = Files.readAllLines(file);

        assertObjectEquals("Статус: обнаружен отрицательный цикл", lines.get(2), "статус цикла");
        assertTrue(lines.contains("A; 0; -; A; Корректно"), "строка источника");
        assertTrue(lines.contains("B; 3; A; A -> B; Корректно (не достижима из цикла)"),
                "строка вершины вне цикла");
        assertTrue(lines.contains("C; -∞; -; -; Не определено (лежит на отрицательном цикле)"),
                "строка вершины на цикле");
        assertTrue(lines.contains("F; -∞; -; -; Не определено (достижима из отрицательного цикла)"),
                "строка вершины, достижимой из цикла");
        assertTrue(lines.contains("G; INF; -; -; Недостижима"), "строка недостижимой вершины");
        printOk("Экспорт: отрицательный цикл со статусами по спецификации");
    }

    // Сценарий ручного редактирования: операции модели, которые вызывает VertexDialog.
    private static void testGraphEditingScenario() {
        Graph graph = new Graph();
        graph.addVertex("V1", 100, 100);
        graph.addVertex("V2", 200, 200);
        graph.addEdge("V1", "V2", 10);
        graph.setSource("V1");

        // Черновик, как в VertexDialog: копия -> правки -> replaceWith.
        Graph draft = new Graph();
        draft.replaceWith(graph);
        draft.addVertex("V3", 300, 300);
        draft.addEdge("V2", "V3", -4);
        draft.renameVertex("V2", "Middle");
        graph.replaceWith(draft);

        assertIntEquals(3, graph.getVertexCount(), "вершины после редактирования");
        assertTrue(graph.hasVertex("Middle"), "переименование применилось");
        assertTrue(!graph.hasVertex("V2"), "старое имя удалено");

        StepHistory history = StepHistory.fromGraph(graph);
        StepState finalStep = history.asList().get(history.size() - 1);
        assertIntEquals(6, finalStep.getDistances().get("V3"), "dist[V3] = 10 + (-4)");

        graph.removeVertex("Middle");
        assertIntEquals(2, graph.getVertexCount(), "удаление вершины");
        assertIntEquals(0, graph.getEdgeCount(), "инцидентные ребра удалены вместе с вершиной");
        printOk("Редактирование графа: черновик, переименование, удаление");
    }

    private static void printOk(String caseName) {
        System.out.println("[OK] " + caseName);
    }

    private static void assertObjectEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": ожидалось " + expected + ", получено " + actual);
        }
    }

    private static void assertIntEquals(int expected, Integer actual, String message) {
        if (actual == null || actual != expected) {
            throw new AssertionError(message + ": ожидалось " + expected + ", получено " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
