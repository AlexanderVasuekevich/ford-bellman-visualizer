package visualizer.parser;

import visualizer.model.Graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Читает граф из текстового файла.
 */
public final class GraphParser {
    private static final String SOURCE = "SOURCE";
    private static final String VERTEX = "VERTEX";
    private static final String EDGE = "EDGE";

    private GraphParser() {
    }

    /**
     * Читает файл, проверяет его корректность и создает объект графа.
     *
     * @param path путь к входному файлу
     * @return Graph
     * @throws GraphParseException если возникает ошибка при чтении файла
     */
    public static Graph parse(Path path) throws GraphParseException {
        validatePath(path);

        List<ParsedLine> lines = readLines(path);
        if (lines.isEmpty()) {
            throw new GraphParseException("Файл пустой.");
        }

        Graph graph = new Graph();
        String sourceName = null;

        for (ParsedLine line : lines) {
            switch (line.command()) {
                case SOURCE -> {
                    validateArgumentCount(line, 1);
                    if (sourceName != null) {
                        throw new GraphParseException(line.number(), "стартовая вершина уже указана.");
                    }
                    sourceName = line.argument(0);
                }
                case VERTEX -> {
                    validateArgumentCount(line, 1);
                    String vertexName = line.argument(0);
                    if (graph.hasVertex(vertexName)) {
                        throw new GraphParseException(line.number(), "вершина " + vertexName + " уже объявлена.");
                    }
                    graph.addVertex(vertexName);
                }
                case EDGE -> validateArgumentCount(line, 3);
                default -> throw new GraphParseException(line.number(), "неизвестная команда " + line.command() + ".");
            }
        }

        if (sourceName == null) {
            throw new GraphParseException("В файле отсутствует стартовая вершина SOURCE.");
        }
        if (graph.getVertexCount() == 0) {
            throw new GraphParseException("В файле отсутствуют вершины VERTEX.");
        }
        if (!graph.hasVertex(sourceName)) {
            throw new GraphParseException("Стартовая вершина " + sourceName + " не объявлена.");
        }

        graph.setSource(sourceName);

        for (ParsedLine line : lines) {
            if (!EDGE.equals(line.command())) {
                continue;
            }

            String fromName = line.argument(0);
            String toName = line.argument(1);
            int weight = parseWeight(line);

            if (!graph.hasVertex(fromName)) {
                throw new GraphParseException(line.number(), "вершина " + fromName + " не объявлена.");
            }
            if (!graph.hasVertex(toName)) {
                throw new GraphParseException(line.number(), "вершина " + toName + " не объявлена.");
            }

            graph.addEdge(fromName, toName, weight);
        }

        return graph;
    }

    private static void validatePath(Path path) throws GraphParseException {
        if (path == null) {
            throw new GraphParseException("Путь к файлу не указан.");
        }
        if (!Files.exists(path)) {
            throw new GraphParseException("Файл не найден: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new GraphParseException("Указанный путь не является файлом: " + path);
        }
    }

    private static List<ParsedLine> readLines(Path path) throws GraphParseException {
        List<String> rawLines;
        try {
            rawLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GraphParseException("Не удалось прочитать файл: " + e.getMessage());
        }

        List<ParsedLine> result = new ArrayList<>();
        for (int i = 0; i < rawLines.size(); i++) {
            String trimmed = rawLines.get(i).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            result.add(parseLine(i + 1, trimmed));
        }
        return result;
    }

    private static ParsedLine parseLine(int number, String text) {
        String[] tokens = text.split("\\s+");
        String command = tokens[0];
        String[] arguments = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, arguments, 0, arguments.length);
        return new ParsedLine(number, command, arguments);
    }

    private static void validateArgumentCount(ParsedLine line, int expected) throws GraphParseException {
        if (line.arguments().length != expected) {
            throw new GraphParseException(
                    line.number(),
                    "команда " + line.command() + " ожидает " + expected
                            + " аргумент(а), получено " + line.arguments().length + "."
            );
        }
    }

    private static int parseWeight(ParsedLine line) throws GraphParseException {
        try {
            return Integer.parseInt(line.argument(2));
        } catch (NumberFormatException e) {
            throw new GraphParseException(line.number(), "вес ребра должен быть целым числом.");
        }
    }

    private record ParsedLine(int number, String command, String[] arguments) {
        private String argument(int index) {
            return arguments[index];
        }
    }
}
