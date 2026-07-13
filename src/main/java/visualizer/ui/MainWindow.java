package visualizer.ui;

import visualizer.model.Graph;
import visualizer.model.PrototypeGraphMock;
import visualizer.model.Vertex;

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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Главное окно приложения «Визуализатор алгоритма Форда–Беллмана».
 *
 * Этап: ПРОТОТИП. Ответственный за окно: Васюкевич Александр (UI, интеграция).
 *
 * Окно объединяет части всей бригады:
 *   - вёрстка окна и панель управления — Васюкевич;
 *   - отрисовка графа (GraphPanel) — Стрижков;
 *   - модель данных и мок-граф (Graph, PrototypeGraphMock) — Бурменский.
 *
 * Логика алгоритма пока не подключена: большинство кнопок без обработчиков
 * (нажатия ничего не выполняют). Работает кнопка «О разработчиках».
 * Таблица расстояний заполнена по мок-графу (расстояния ещё не вычислены).
 */
public class MainWindow extends JFrame {

    // --- Элементы управления (панель сверху) ---
    private JButton loadButton;
    private JButton saveButton;
    private JButton runButton;
    private JButton prevButton;
    private JButton nextButton;
    private JToggleButton autoButton;
    private JTextField intervalField;
    private JToggleButton editButton;
    private JButton aboutButton;

    // --- Центр / право / низ ---
    private GraphPanel graphPanel;
    private JTable distanceTable;
    private JTextArea explanationArea;

    // --- Индикаторы состояния ---
    private JLabel passLabel;
    private JLabel stepLabel;

    // Мок-граф из модели данных (Бурменский) — показывается на прототипе.
    private final Graph graph = PrototypeGraphMock.createGraph();

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
     * Кроме «О разработчиках», обработчики не привязаны (этап прототипа).
     */
    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        loadButton = new JButton("Загрузить файл");
        saveButton = new JButton("Сохранить в файл");
        saveButton.setToolTipText("Сохранить результат работы алгоритма в файл (реализуется в версии 2)");
        runButton = new JButton("Запустить");
        prevButton = new JButton("← Назад");
        nextButton = new JButton("Вперёд →");
        autoButton = new JToggleButton("Авто");
        editButton = new JToggleButton("Редактировать");
        aboutButton = new JButton("О разработчиках");

        // Единственный подключённый обработчик на прототипе —
        // модальное окно «О разработчиках» (класс AboutDialog, Стрижков).
        aboutButton.addActionListener(e -> AboutDialog.show(this));

        intervalField = new JTextField("1000", 5);
        intervalField.setMaximumSize(new Dimension(60, 28));
        intervalField.setToolTipText("Интервал автоматического режима, мс");
        JLabel intervalLabel = new JLabel("Интервал, мс:");

        toolBar.add(loadButton);
        toolBar.add(saveButton);
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
     * Центральная часть: слева — область графа (GraphPanel Стрижкова,
     * отрисовывает мок-граф) с прокруткой; справа — таблица расстояний.
     */
    private JSplitPane buildCenterAndRight() {
        graphPanel = new GraphPanel(graph);
        JScrollPane graphScroll = new JScrollPane(
                graphPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setBorder(BorderFactory.createTitledBorder("Граф"));

        distanceTable = new JTable(buildDistanceModel());
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
     * Таблица расстояний строится по вершинам мок-графа. Расстояния ещё не
     * вычислены (алгоритм не подключён): у источника — 0, у остальных — «—».
     */
    private DefaultTableModel buildDistanceModel() {
        String[] columns = {"Vertex", "Distance", "Parent"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // таблица только для просмотра
            }
        };
        Vertex source = graph.getSource();
        for (Vertex v : graph.getVertices()) {
            boolean isSource = (v == source);
            model.addRow(new Object[]{
                    v.getName(),
                    isSource ? "0" : "—",
                    isSource ? "-" : "—"
            });
        }
        return model;
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
                + "На этапе прототипа алгоритм ещё не подключён — показан исходный граф.");

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setBorder(BorderFactory.createTitledBorder("Пояснение шага"));

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
