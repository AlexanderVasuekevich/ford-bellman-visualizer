package visualizer.parser;

/**
 * Ошибка чтения или проверки входного файла с описанием графа.
 */
public class GraphParseException extends Exception {
    /**
     * Создает ошибку без привязки к конкретной строке файла.
     *
     * @param message текст ошибки
     */
    public GraphParseException(String message) {
        super(message);
    }

    /**
     * Создает ошибку с номером строки входного файла.
     *
     * @param lineNumber номер строки, начиная с 1
     * @param message текст ошибки
     */
    public GraphParseException(int lineNumber, String message) {
        super("Ошибка в строке " + lineNumber + ": " + message);
    }
}
