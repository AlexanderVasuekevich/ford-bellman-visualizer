package visualizer.algorithm;

import visualizer.model.Edge;

/**
 * Формирует текстовые пояснения для шагов алгоритма Форда-Беллмана.
 */
final class StepExplanationBuilder {
    private StepExplanationBuilder() {
    }

    static String initial(String sourceName) {
        return "Инициализация.\n"
                + "Стартовая вершина: " + sourceName + ".\n"
                + "dist[" + sourceName + "] = 0.\n"
                + "Расстояния до остальных вершин = INF.";
    }

    static String relaxation(
            int stepNumber,
            int passNumber,
            int maxPass,
            Edge edge,
            BellmanFord.RelaxationResult result
    ) {
        String fromName = edge.getFrom().getName();
        String toName = edge.getTo().getName();
        int weight = edge.getWeight();

        StringBuilder text = new StringBuilder();
        text.append("Проход ").append(passNumber).append(" из ").append(maxPass).append(".\n");
        text.append("Шаг ").append(stepNumber).append(".\n");
        text.append("Рассматривается ребро ").append(fromName).append(" -> ").append(toName)
                .append(" с весом ").append(weight).append(".\n");
        text.append("dist[").append(fromName).append("] = ").append(formatDistance(result.fromDistance())).append("\n");
        text.append("dist[").append(toName).append("] = ").append(formatDistance(result.oldToDistance())).append("\n");

        if (result.fromDistance() == BellmanFord.INF) {
            text.append("Проверка невозможна: начальная вершина ребра пока недостижима.\n");
            text.append("Расстояние до вершины ").append(toName).append(" не изменяется.");
            return text.toString();
        }

        text.append("Проверка:\n");
        text.append(result.fromDistance()).append(" + (").append(weight).append(") = ")
                .append(result.candidateDistance()).append("\n");

        if (result.updated()) {
            text.append(result.candidateDistance()).append(" < ").append(formatDistance(result.oldToDistance()))
                    .append(", расстояние до вершины ").append(toName).append(" обновляется.\n");
            text.append("Новое значение dist[").append(toName).append("] = ")
                    .append(result.candidateDistance()).append(".\n");
            text.append("Предыдущая вершина для ").append(toName).append(" = ").append(fromName).append(".");
        } else {
            text.append("Так как ").append(result.candidateDistance()).append(" не меньше ")
                    .append(formatDistance(result.oldToDistance()))
                    .append(", расстояние до вершины ").append(toName).append(" не изменяется.");
        }

        return text.toString();
    }

    static String negativeCycleCheck(
            int stepNumber,
            int passNumber,
            Edge edge,
            BellmanFord.RelaxationCheck check
    ) {
        String fromName = edge.getFrom().getName();
        String toName = edge.getTo().getName();
        Integer candidate = check.candidateDistance();
        int weight = edge.getWeight();

        StringBuilder text = new StringBuilder();
        text.append("Проход ").append(passNumber).append(".\n");
        text.append("Шаг ").append(stepNumber).append(".\n");
        text.append("Проверка отрицательного цикла.\n");
        text.append("Рассматривается ребро ").append(fromName).append(" -> ").append(toName)
                .append(" с весом ").append(weight).append(".\n");
        text.append("dist[").append(fromName).append("] = ").append(formatDistance(check.fromDistance())).append("\n");
        text.append("dist[").append(toName).append("] = ").append(formatDistance(check.toDistance())).append("\n");

        if (check.fromDistance() == BellmanFord.INF) {
            text.append("Проверка невозможна: начальная вершина ребра недостижима.\n");
            text.append("На этом ребре отрицательный цикл не обнаружен.");
            return text.toString();
        }

        text.append("Проверка:\n");
        text.append(check.fromDistance()).append(" + (").append(weight).append(") = ")
                .append(candidate).append("\n");

        if (check.canRelax()) {
            text.append(candidate).append(" < ").append(formatDistance(check.toDistance()))
                    .append(", значит после основных проходов расстояние все еще можно уменьшить.\n");
            text.append("Обнаружен отрицательный цикл, достижимый из стартовой вершины.");
        } else {
            text.append("Так как ").append(candidate).append(" не меньше ")
                    .append(formatDistance(check.toDistance()))
                    .append(", на этом ребре отрицательный цикл не обнаружен.");
        }

        return text.toString();
    }

    static String finished() {
        return "Алгоритм завершен. Отрицательный цикл не обнаружен.";
    }

    private static String formatDistance(int distance) {
        return distance == BellmanFord.INF ? "INF" : String.valueOf(distance);
    }
}
