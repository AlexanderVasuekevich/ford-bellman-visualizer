package visualizer.export;

import visualizer.algorithm.BellmanFord;
import visualizer.algorithm.StepHistory;
import visualizer.algorithm.StepState;
import visualizer.model.Graph;
import visualizer.model.Vertex;

import java.nio.file.AccessDeniedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сохраняет результат работы алгоритма Форда-Беллмана в текстовый файл.
 */
public final class AlgorithmResultExporter {
    private AlgorithmResultExporter() {
    }

    /**
     * Сохраняет результат алгоритма в файл.
     *
     * @param graph исходный граф
     * @param history история шагов алгоритма
     * @param path путь для сохранения файла
     * @throws ResultOutputException если файл не удалось записать
     */
    public static void save(Graph graph, StepHistory history, Path path) throws ResultOutputException {
        validateOutputPath(path);

        try {
            Files.writeString(path, buildText(graph, history), StandardCharsets.UTF_8);
        } catch (AccessDeniedException e) {
            throw new ResultOutputException("Нет прав на запись в файл: " + path, e);
        } catch (Exception e) {
            throw new ResultOutputException("Не удалось сохранить файл: " + e.getMessage(), e);
        }
    }

    private static String buildText(Graph graph, StepHistory history) {
        validateInput(graph, history);

        StepState finalStep = getFinalStep(history);
        boolean hasNegativeCycle = finalStep.hasNegativeCycle();
        StringBuilder text = new StringBuilder();

        text.append("Алгоритм: Форда-Беллмана\n");
        text.append("Источник: ").append(graph.getSource().getName()).append("\n");

        if (hasNegativeCycle) {
            appendNegativeCycleResult(text, graph, finalStep);
        } else {
            appendSuccessfulResult(text, graph, finalStep);
        }

        return text.toString();
    }

    private static void appendSuccessfulResult(StringBuilder text, Graph graph, StepState finalStep) {
        text.append("Статус: успешно завершено\n");
        text.append("Вершина; Расстояние; Предыдущая вершина; Путь\n");

        Map<String, Integer> distances = finalStep.getDistances();
        Map<String, String> predecessors = finalStep.getPredecessors();

        for (Vertex vertex : graph.getVertices()) {
            String name = vertex.getName();
            text.append(name).append("; ")
                    .append(formatDistance(distances.get(name))).append("; ")
                    .append(formatParent(predecessors.get(name))).append("; ")
                    .append(buildPath(name, distances, predecessors))
                    .append("\n");
        }
    }

    private static void appendNegativeCycleResult(StringBuilder text, Graph graph, StepState finalStep) {
        text.append("Статус: обнаружен отрицательный цикл\n");
        text.append("Кратчайшие пути не могут быть корректно определены для всех вершин.\n");
        text.append("Вершина; Расстояние; Предыдущая вершина; Путь; Статус\n");

        Map<String, Integer> distances = finalStep.getDistances();
        Map<String, String> predecessors = finalStep.getPredecessors();
        Set<String> affectedVertices = finalStep.getNegativeCycleAffectedVertices();

        for (Vertex vertex : graph.getVertices()) {
            String name = vertex.getName();
            boolean affected = affectedVertices.contains(name);
            boolean unreachable = isUnreachable(distances.get(name));

            text.append(name).append("; ");
            if (affected) {
                text.append("-∞; -; -; Не определено");
            } else if (unreachable) {
                text.append("INF; -; -; Недостижима");
            } else {
                text.append(formatDistance(distances.get(name))).append("; ")
                        .append(formatParent(predecessors.get(name))).append("; ")
                        .append(buildPath(name, distances, predecessors)).append("; ")
                        .append("Корректно");
            }
            text.append("\n");
        }
    }

    private static void validateInput(Graph graph, StepHistory history) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }
        if (graph.getSource() == null) {
            throw new IllegalArgumentException("Graph source must be set");
        }
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("Step history must not be empty");
        }
    }

    private static void validateOutputPath(Path path) throws ResultOutputException {
        if (path == null) {
            throw new ResultOutputException("Путь для сохранения не указан.");
        }
        if (Files.isDirectory(path)) {
            throw new ResultOutputException("Указанный путь является директорией: " + path);
        }

        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            if (!Files.exists(parent)) {
                throw new ResultOutputException("Папка для сохранения не существует: " + parent);
            }
            if (!Files.isDirectory(parent)) {
                throw new ResultOutputException("Родительский путь не является папкой: " + parent);
            }
            if (!Files.isWritable(parent)) {
                throw new ResultOutputException("Нет прав на запись в папку: " + parent);
            }
        }

        if (Files.exists(path) && !Files.isWritable(path)) {
            throw new ResultOutputException("Нет прав на запись в файл: " + path);
        }
    }

    private static StepState getFinalStep(StepHistory history) {
        List<StepState> steps = history.asList();
        return steps.get(steps.size() - 1);
    }

    private static String buildPath(
            String vertexName,
            Map<String, Integer> distances,
            Map<String, String> predecessors
    ) {
        if (isUnreachable(distances.get(vertexName))) {
            return "-";
        }

        List<String> reversedPath = new ArrayList<>();
        String current = vertexName;
        while (current != null) {
            reversedPath.add(current);
            current = predecessors.get(current);
        }

        List<String> path = new ArrayList<>();
        for (int i = reversedPath.size() - 1; i >= 0; i--) {
            path.add(reversedPath.get(i));
        }
        return String.join(" -> ", path);
    }

    private static String formatDistance(Integer distance) {
        return isUnreachable(distance) ? "INF" : String.valueOf(distance);
    }

    private static String formatParent(String parent) {
        return parent == null ? "-" : parent;
    }

    private static boolean isUnreachable(Integer distance) {
        return distance == null || distance == BellmanFord.INF;
    }
}
