package parser;

import visualizer.model.Graph;
import visualizer.parser.GraphParseException;
import visualizer.parser.GraphParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Проверяет чтение графа из файлов в формате SOURCE / VERTEX / EDGE.
 *
 * Запуск из корня репозитория:
 * javac -d /tmp/ford-bellman-test-classes $(find src/main/java tests -name '*.java')
 * java -cp /tmp/ford-bellman-test-classes parser.GraphFileParserTest
 */
public final class GraphFileParserTest {
    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();
    private static final Path VALID_DATA_DIR = PROJECT_ROOT.resolve("test-data/valid");
    private static final Path INVALID_DATA_DIR = PROJECT_ROOT.resolve("test-data/invalid");
    private static final Map<String, String> VALID_CASE_NAMES = Map.of(
            "bidirectional-graph.txt", "Корректный файл: встречные ребра",
            "bigger-graph.txt", "Корректный файл: большой граф",
            "disconnected-graph.txt", "Корректный файл: несвязный граф",
            "negative-cycle.txt", "Корректный файл: отрицательный цикл",
            "negative-edges.txt", "Корректный файл: отрицательные ребра",
            "simple-graph.txt", "Корректный файл: простой граф"
    );
    private static final Map<String, String> INVALID_CASE_NAMES = Map.of(
            "duplicate-vertex.txt", "Дубликат вершины",
            "edge-unknown-from.txt", "Ребро из несуществующей вершины",
            "edge-unknown-to.txt", "Ребро в несуществующую вершину",
            "empty-file.txt", "Пустой файл",
            "missing-source.txt", "Отсутствует SOURCE",
            "no-vertices.txt", "Отсутствуют вершины",
            "non-numeric-weight.txt", "Нечисловой вес ребра",
            "undeclared-source.txt", "Необъявленная стартовая вершина",
            "unknown-command.txt", "Неизвестная команда",
            "wrong-argument-count.txt", "Неверное число аргументов"
    );

    private GraphFileParserTest() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Проверка парсинга графа из файлов");
        System.out.println();
        testValidGraphFiles();
        testInvalidGraphFiles();
        testMissingGraphFile();
        System.out.println();
        System.out.println("Все тесты парсинга графа пройдены.");
    }

    private static void testValidGraphFiles() throws Exception {
        for (Path path : listTextFiles(VALID_DATA_DIR)) {
            String caseName = getCaseName(path, VALID_CASE_NAMES);
            try {
                Graph graph = GraphParser.parse(path);
                assertTrue(graph.getSource() != null, "Source is missing: " + path);
                assertTrue(graph.getVertexCount() > 0, "Vertices are missing: " + path);
                printOk(caseName);
            } catch (Exception e) {
                printFail(caseName, e.getMessage());
                throw e;
            }
        }
    }

    private static void testInvalidGraphFiles() throws Exception {
        for (Path path : listTextFiles(INVALID_DATA_DIR)) {
            String caseName = getCaseName(path, INVALID_CASE_NAMES);
            try {
                GraphParser.parse(path);
                printFail(caseName, "ошибка не была выброшена");
                throw new AssertionError("Invalid file parsed without error: " + path);
            } catch (GraphParseException expected) {
                assertTrue(!expected.getMessage().isBlank(), "Error message is empty: " + path);
                printOk(caseName);
            }
        }
    }

    private static void testMissingGraphFile() {
        Path missingFile = INVALID_DATA_DIR.resolve("not-existing-file.txt");
        String caseName = "Несуществующий файл";
        try {
            GraphParser.parse(missingFile);
            printFail(caseName, "ошибка не была выброшена");
            throw new AssertionError("Missing file parsed without error: " + missingFile);
        } catch (GraphParseException expected) {
            assertTrue(expected.getMessage().contains("Файл не найден"), "Unexpected error: " + expected.getMessage());
            printOk(caseName);
        }
    }

    private static List<Path> listTextFiles(Path directory) throws Exception {
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(path -> path.toString().endsWith(".txt"))
                    .sorted()
                    .toList();
        }
    }

    private static String getCaseName(Path path, Map<String, String> names) {
        String fileName = path.getFileName().toString();
        return names.getOrDefault(fileName, fileName);
    }

    private static void printOk(String caseName) {
        System.out.println("[OK] " + caseName);
    }

    private static void printFail(String caseName, String reason) {
        System.out.println("[FAIL] " + caseName + ": " + reason);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
