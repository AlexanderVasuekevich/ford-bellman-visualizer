package visualizer.ui;

import javax.swing.*;
import java.awt.*;

public class AboutDialog {
    /**
     * Показывает модальное окно с информацией о вашей бригаде
     */
    public static void show(Component parent) {
        String infoText = "Визуализатор алгоритма Форда-Беллмана\n" +
                          "Разработано в рамках учебной практики 2026.\n\n" +
                          "Состав бригады:\n" +
                          " - Васюкевич Александр (UI, интеграция)\n" +
                          " - Бурменский Алексей  (модель данных, парсинг, логика)\n" +
                          " - Стрижков Иван       (визуализация, графика)";

        JOptionPane.showMessageDialog(
            parent, 
            infoText, 
            "О программе", 
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
