package visualizer.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Главное окно приложения «Визуализатор алгоритма Форда–Беллмана».
 *
 * Этап: ПРОТОТИП. Ответственный: Васюкевич Александр (UI и интеграция).
 *
 * Задача этого класса на прототипе — СВЁРСТАТЬ интерфейс:
 *   - панель управления сверху (все кнопки и поле интервала);
 *   - область отображения графа по центру (с прокруткой);
 *   - таблица текущих расстояний справа;
 *   - область текстового пояснения снизу;
 *   - индикаторы текущего прохода и текущего шага.
 *
 * Логика намеренно отсутствует: обработчики к кнопкам НЕ привязаны,
 * нажатия ничего не выполняют. Данные в таблице и пояснении — статические
 * примеры-заглушки, только чтобы показать вид интерфейса. Функциональность
 * добавляется в Alpha-версии.
 *
 * ИНТЕГРАЦИЯ: центральная область сейчас — временный placeholder
 * (метод createGraphPlaceholder). В Alpha он заменяется на GraphPanel
 * Стрижкова: заменить строку в buildCenterAndRight() на
 *     JComponent graph = new GraphPanel();
 */
public class MainWindow extends JFrame {

    // --- Элементы управления (панель сверху) ---
    private JButton loadButton;
    private JButton runButton;
    private JButton prevButton;
    private JButton nextButton;
    private JToggleButton autoButton;
    private JTextField intervalField;
    private JToggleButton editButton;
    private JButton aboutButton;

    // --- Центр / право / низ ---
    private JPanel graphPlaceholder;
    private JTable distanceTable;
    private JTextArea explanationArea;

    // --- Индикаторы состояния ---
    private JLabel passLabel;
    private JLabel stepLabel;

    public MainWindow() {
        super("Визуализатор алгоритма Форда–Беллмана");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildToolBar(), BorderLayout.NORTH);
        add(buildCenterAndRight(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 720);
        setLocationRelativeTo(null); // по центру экрана
    }

    /**
     * Панель управления сверху со всеми кнопками и полем интервала.
     * Кнопки создаются, но обработчики к ним не привязаны.
     */
    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        loadButton = new JButton("Загрузить файл");
        runButton = new JButton("Запустить");
        prevButton = new JButton("← Назад");
        nextButton = new JButton("Вперёд →");
        autoButton = new JToggleButton("Авто");
        editButton = new JToggleButton("Редактировать");
        aboutButton = new JButton("О разработчиках");

        intervalField = new JTextField("1000", 5);
        intervalField.setMaximumSize(new Dimension(60, 28));
        intervalField.setToolTipText("Интервал автоматического режима, мс");
        JLabel intervalLabel = new JLabel("Интервал, мс:");

        toolBar.add(loadButton);
        toolBar.addSeparator();
        toolBar.add(runButton);
        toolBar.add(prevButton);
        toolBar.add(nextButton);
        toolBar.add(autoButton);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(intervalLabel);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(intervalField);
        toolBar.addSeparator();
        toolBar.add(editButton);
        toolBar.add(Box.createHorizontalGlue()); // прижать «О разработчиках» вправо
        toolBar.add(aboutButton);

        return toolBar;
    }

    /**
     * Центральная часть: слева — область графа (с прокруткой),
     * справа — таблица текущих расстояний. Разделены сплиттером.
     */
    private JSplitPane buildCenterAndRight() {
        // ВРЕМЕННО: placeholder вместо графа. В Alpha заменить на GraphPanel Стрижкова.
        graphPlaceholder = createGraphPlaceholder();
        JScrollPane graphScroll = new JScrollPane(
                graphPlaceholder,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setBorder(BorderFactory.createTitledBorder("Граф"));

        // Таблица расстояний. Данные — статический пример (заглушка).
        String[] columns = {"Vertex", "Distance", "Parent"};
        Object[][] sampleRows = {
                {"A", "0", "-"},
                {"B", "—", "—"},
                {"C", "—", "—"},
                {"D", "—", "—"},
        };
        DefaultTableModel model = new DefaultTableModel(sampleRows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // таблица только для просмотра
            }
        };
        distanceTable = new JTable(model);
        distanceTable.setRowHeight(24);
        distanceTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(
                distanceTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Таблица расстояний"));
        tableScroll.setPreferredSize(new Dimension(260, 400));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                graphScroll, tableScroll);
        split.setResizeWeight(1.0);   // при растягивании окна растёт область графа
        split.setDividerLocation(820);
        return split;
    }

    /**
     * Временная заглушка центральной области.
     * Показывает рамку и поясняющую надпись; в Alpha заменяется на GraphPanel.
     */
    private JPanel createGraphPlaceholder() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(0x99, 0x99, 0x99));
                g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
                String s1 = "Область отображения графа";
                String s2 = "(отрисовка — задача визуализации, класс GraphPanel)";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s1, w / 2 - fm.stringWidth(s1) / 2, h / 2 - 6);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
                fm = g2.getFontMetrics();
                g2.drawString(s2, w / 2 - fm.stringWidth(s2) / 2, h / 2 + 16);
                g2.dispose();
            }
        };
        panel.setBackground(Color.WHITE);
        // Размер больше окна — чтобы на прототипе были видны ползунки прокрутки.
        panel.setPreferredSize(new Dimension(900, 700));
        return panel;
    }

    /**
     * Нижняя часть: область текстового пояснения текущего шага
     * и строка состояния с номерами прохода и шага.
     */
    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());

        explanationArea = new JTextArea(4, 20);
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(explanationArea.getFont().deriveFont(Font.PLAIN, 13f));
        explanationArea.setText("Здесь будет отображаться пояснение текущего шага алгоритма.\n"
                + "На этапе прототипа это область-заглушка без данных.");

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setBorder(BorderFactory.createTitledBorder("Пояснение шага"));

        // Строка состояния: текущий проход и текущий шаг.
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        passLabel = new JLabel("Проход: 0 из 0");
        stepLabel = new JLabel("Шаг: 0");
        passLabel.setFont(passLabel.getFont().deriveFont(Font.BOLD));
        stepLabel.setFont(stepLabel.getFont().deriveFont(Font.BOLD));
        status.add(passLabel);
        status.add(new JLabel("|"));
        status.add(stepLabel);

        bottom.add(explanationScroll, BorderLayout.CENTER);
        bottom.add(status, BorderLayout.SOUTH);
        bottom.setPreferredSize(new Dimension(100, 150));
        return bottom;
    }
}
