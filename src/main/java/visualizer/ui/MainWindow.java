package visualizer.ui;

import visualizer.algorithm.AlgorithmStep;
import visualizer.algorithm.BellmanFord;
import visualizer.model.Graph;
import visualizer.model.Vertex;
import visualizer.parser.GraphParseException;
import visualizer.parser.GraphParser;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Главное окно приложения «Визуализатор алгоритма Форда–Беллмана».
 *
 * Этап: ВЕРСИЯ 1. Ответственный за окно и интеграцию: Васюкевич Александр.
 *
 * Что делает это окно на Версии 1:
 *   - «Загрузить файл» — выбор .txt, разбор через GraphParser, показ графа
 *     (GraphPanel) и таблицы расстояний (DistanceTable); ошибки файла
 *     выводятся через MessageDialog;
 *   - «Запустить» — запуск алгоритма BellmanFord на загруженном графе, вывод
 *     итогового результата и простейшего лога работы в текстовую область,
 *     подсветка финальных расстояний; отрицательный цикл перехватывается и
 *     показывается через MessageDialog (приложение не падает);
 *   - связывает модули: парсер → модель → отрисовка/таблица → алгоритм.
 *
 * Пошаговая навигация (Назад/Вперёд/Авто), сохранение в файл и режим
 * редактирования — это Версия 2 и финал, поэтому соответствующие кнопки
 * пока отключены.
 */
public class MainWindow extends JFrame {

    /** Значение «бесконечности», согласованное с BellmanFord/DistanceTable. */
    private static final int INF = 1_000_000_000;

    // --- Панель управления ---
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
    private JScrollPane graphScroll;
    private DistanceTable distanceTable;
    private JScrollPane tableScroll;
    private JTextArea explanationArea;
    private JLabel passLabel;
    private JLabel stepLabel;

    // Текущий загруженный граф (null, пока файл не загружен).
    private Graph currentGraph;

    public MainWindow() {
        super("Визуализатор алгоритма Форда–Беллмана");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildToolBar(), BorderLayout.NORTH);
        add(buildCenterAndRight(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 720);
        setLocationRelativeTo(null);
        runButton.setEnabled(false); // до загрузки графа запускать нечего
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        loadButton = new JButton("Загрузить файл");
        saveButton = new JButton("Сохранить в файл");
        runButton = new JButton("Запустить");
        prevButton = new JButton("← Назад");
        nextButton = new JButton("Вперёд →");
        autoButton = new JToggleButton("Авто");
        editButton = new JToggleButton("Редактировать");
        aboutButton = new JButton("О разработчиках");

        intervalField = new JTextField("1000", 5);
        intervalField.setMaximumSize(new Dimension(60, 28));
        JLabel intervalLabel = new JLabel("Интервал, мс:");

        // Функции Версии 2 / финала — пока отключены.
        String v2 = "Реализуется в версии 2";
        saveButton.setEnabled(false); saveButton.setToolTipText(v2);
        prevButton.setEnabled(false); prevButton.setToolTipText(v2);
        nextButton.setEnabled(false); nextButton.setToolTipText(v2);
        autoButton.setEnabled(false); autoButton.setToolTipText(v2);
        intervalField.setEnabled(false); intervalField.setToolTipText(v2);
        editButton.setEnabled(false); editButton.setToolTipText("Реализуется в финальной версии");

        // Обработчики Версии 1.
        loadButton.addActionListener(e -> chooseAndLoad());
        runButton.addActionListener(e -> runAlgorithm());
        aboutButton.addActionListener(e -> AboutDialog.show(this));

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
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(aboutButton);

        return toolBar;
    }

    private JSplitPane buildCenterAndRight() {
        graphPanel = new GraphPanel();
        graphScroll = new JScrollPane(
                graphPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphScroll.setBorder(BorderFactory.createTitledBorder("Граф"));

        // Пустая таблица до загрузки графа (пустой граф — строк нет).
        distanceTable = new DistanceTable(new Graph());
        tableScroll = new JScrollPane(
                distanceTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Таблица расстояний"));
        tableScroll.setPreferredSize(new Dimension(260, 400));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphScroll, tableScroll);
        split.setResizeWeight(1.0);
        split.setDividerLocation(820);
        return split;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());

        explanationArea = new JTextArea(6, 20);
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        explanationArea.setText("Нажмите «Загрузить файл», чтобы открыть граф, затем «Запустить».");

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setBorder(BorderFactory.createTitledBorder("Результат и лог работы"));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        passLabel = new JLabel("Проходов: 0");
        stepLabel = new JLabel("Шагов: 0");
        passLabel.setFont(passLabel.getFont().deriveFont(Font.BOLD));
        stepLabel.setFont(stepLabel.getFont().deriveFont(Font.BOLD));
        status.add(passLabel);
        status.add(new JLabel("|"));
        status.add(stepLabel);

        bottom.add(explanationScroll, BorderLayout.CENTER);
        bottom.add(status, BorderLayout.SOUTH);
        bottom.setPreferredSize(new Dimension(100, 190));
        return bottom;
    }

    /** Открывает диалог выбора файла и загружает выбранный граф. */
    private void chooseAndLoad() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл с графом");
        chooser.setFileFilter(new FileNameExtensionFilter("Текстовый файл графа (*.txt)", "txt"));
        File testData = new File("test-data");
        if (testData.isDirectory()) {
            chooser.setCurrentDirectory(testData);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadGraph(chooser.getSelectedFile().toPath());
        }
    }

    /**
     * Загружает граф из файла и обновляет интерфейс. Ошибки разбора файла
     * показываются через модальное окно и не роняют приложение.
     *
     * @param path путь к файлу с описанием графа
     */
    public void loadGraph(Path path) {
        try {
            Graph graph = GraphParser.parse(path);
            currentGraph = graph;

            // Заново создаём панель графа и таблицу под новый граф.
            graphPanel = new GraphPanel(graph);
            graphScroll.setViewportView(graphPanel);

            distanceTable = new DistanceTable(graph);
            tableScroll.setViewportView(distanceTable);

            String source = graph.getSource().getName();
            explanationArea.setText(
                    "Граф загружен: вершин — " + graph.getVertexCount()
                            + ", рёбер — " + graph.getEdgeCount()
                            + ", источник — " + source + ".\n"
                            + "Нажмите «Запустить» для выполнения алгоритма.");
            passLabel.setText("Проходов: 0");
            stepLabel.setText("Шагов: 0");
            runButton.setEnabled(true);
        } catch (GraphParseException ex) {
            MessageDialog.show(this, "Ошибка загрузки файла", ex.getMessage());
        }
    }

    /**
     * Запускает алгоритм Форда–Беллмана на текущем графе и выводит итоговый
     * результат и лог работы. Отрицательный цикл перехватывается и
     * показывается пользователю, приложение при этом не падает.
     */
    public void runAlgorithm() {
        if (currentGraph == null) {
            MessageDialog.show(this, "Граф не загружен", "Сначала загрузите файл с графом.");
            return;
        }
        try {
            BellmanFord bellmanFord = new BellmanFord();
            List<Vertex> vertices = new ArrayList<>(currentGraph.getVertices());
            List<AlgorithmStep> steps =
                    bellmanFord.run(vertices, currentGraph.getEdges(), currentGraph.getSource());

            Map<String, Integer> dist = bellmanFord.getDist();
            Map<String, String> pred = bellmanFord.getPredecessors();

            // Финальная подсветка на графе и обновление таблицы.
            graphPanel.setDistances(dist);
            graphPanel.setPredecessors(pred);
            graphPanel.clearHighlight();
            if (!steps.isEmpty()) {
                distanceTable.refreshTable(steps.get(steps.size() - 1));
            }

            explanationArea.setText(buildReport(dist, pred, steps));
            explanationArea.setCaretPosition(0);

            int passes = 0;
            for (AlgorithmStep s : steps) {
                passes = Math.max(passes, s.getPassNumber());
            }
            passLabel.setText("Проходов: " + passes);
            stepLabel.setText("Шагов: " + steps.size());
        } catch (RuntimeException ex) {
            // BellmanFord бросает IllegalArgumentException при отрицательном цикле.
            MessageDialog.show(this, "Ошибка алгоритма",
                    ex.getMessage() != null ? ex.getMessage() : "Не удалось выполнить алгоритм.");
        }
    }

    /** Формирует текст с итоговым результатом и простейшим логом работы. */
    private String buildReport(Map<String, Integer> dist, Map<String, String> pred,
                               List<AlgorithmStep> steps) {
        StringBuilder sb = new StringBuilder();
        sb.append("Алгоритм: Форда–Беллмана\n");
        sb.append("Источник: ").append(currentGraph.getSource().getName()).append("\n");
        sb.append("Статус: успешно завершено\n\n");

        sb.append("Итоговые расстояния:\n");
        sb.append("Вершина; Расстояние; Предыдущая; Путь\n");
        for (Vertex v : currentGraph.getVertices()) {
            String name = v.getName();
            Integer d = dist.get(name);
            String distStr = (d == null || d >= INF) ? "INF" : String.valueOf(d);
            String parent = pred.get(name) == null ? "-" : pred.get(name);
            sb.append(name).append("; ").append(distStr).append("; ")
                    .append(parent).append("; ").append(buildPath(name, dist, pred)).append("\n");
        }

        sb.append("\nЛог работы (").append(steps.size()).append(" шагов):\n");
        for (AlgorithmStep step : steps) {
            if (step.getEdgeIndex() < 0) {
                sb.append("• ").append(step.getExplanation()).append("\n");
            } else {
                sb.append("• [проход ").append(step.getPassNumber()).append("] ")
                        .append(step.getExplanation()).append("\n");
            }
        }
        return sb.toString();
    }

    /** Восстанавливает путь от источника до вершины по предшественникам. */
    private String buildPath(String vertex, Map<String, Integer> dist, Map<String, String> pred) {
        Integer d = dist.get(vertex);
        if (d == null || d >= INF) {
            return "—";
        }
        LinkedList<String> path = new LinkedList<>();
        String current = vertex;
        int guard = 0;
        while (current != null && guard++ < 100000) {
            path.addFirst(current);
            current = pred.get(current);
        }
        return String.join(" → ", path);
    }
}
