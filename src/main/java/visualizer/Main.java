package visualizer;

import visualizer.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Точка входа приложения «Визуализатор алгоритма Форда–Беллмана».
 *
 * Этап: ПРОТОТИП. Ответственный: Васюкевич Александр (UI и интеграция).
 *
 * На этапе прототипа приложение только создаёт и показывает главное окно
 * со всей вёрсткой интерфейса. Логика (загрузка файла, работа алгоритма,
 * обработчики кнопок) намеренно отсутствует — она появится в Alpha-версии.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Пытаемся включить системный вид интерфейса; если не вышло — стандартный.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Оставляем оформление по умолчанию.
        }

        // Все операции со Swing выполняем в потоке диспетчеризации событий (EDT).
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
