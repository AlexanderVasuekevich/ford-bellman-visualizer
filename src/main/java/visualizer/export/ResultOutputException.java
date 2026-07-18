package visualizer.export;

/**
 * Ошибка сохранения результата работы алгоритма в файл.
 */
public class ResultOutputException extends Exception {
    /**
     * Создает ошибку сохранения результата.
     *
     * @param message текст ошибки
     */
    public ResultOutputException(String message) {
        super(message);
    }

    /**
     * Создает ошибку сохранения результата с исходной причиной.
     *
     * @param message текст ошибки
     * @param cause исходная ошибка
     */
    public ResultOutputException(String message, Throwable cause) {
        super(message, cause);
    }
}
