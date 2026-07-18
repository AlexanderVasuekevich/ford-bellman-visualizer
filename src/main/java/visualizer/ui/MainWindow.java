package visualizer.ui;

import visualizer.algorithm.StepHistory;
import visualizer.algorithm.StepState;
import visualizer.export.AlgorithmResultExporter;
import visualizer.export.ResultOutputException;
import visualizer.model.Graph;
import visualizer.parser.GraphParseException;
import visualizer.parser.GraphParser;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Главное окно приложения «Визуализатор алгоритма Форда–Беллмана».
 *
 * Этап: FINAL. Ответственный за окно и интеграцию: Васюкевич Александр.
 *
 * Возможности финальной версии (моя часть):
 *   - «Запустить» — строит историю шагов (StepHistory Бурменского) и
 *     показывает начальное состояние;
 *   - «Вперёд» / «Назад» — пошаговый проход истории с обновлением подсветки
 *     графа, таблицы, пояснения и номеров прохода/шага;
 *   - «Авто» — автоматический проход шагов через javax.swing.Timer с
 *     интервалом из поля «Интервал, мс» (интервал применяется на лету);
 *   - «Сохранить в файл» — экспорт результата (AlgorithmResultExporter);
 *   - «Редактировать» — режим ручного редактирования графа: клик по
 *     свободному месту создаёт вершину, клик по вершине открывает окно
 *     редактирования (VertexDialog, логика черновика — Бурменского);
 *   - отрицательный цикл и ошибки файла обрабатываются без падения программы.
 *
 * Шаги строятся и хранятся в StepHistory (по просьбе Бурменского).
 */
public class MainWindow extends JFrame {

    private static final int MIN_INTERVAL_MS = 100;
    private static final int DEFAULT_INTERVAL_MS = 1000;

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

    // --- Центр / низ ---
    private JPanel centerHolder;
    private MainSplitPane split;
    private JTextArea explanationArea;
    private JLabel passLabel;
    private JLabel stepLabel;

    // --- Состояние ---
    private Graph currentGraph;
    private StepHistory history;
    private Timer autoTimer;

    public MainWindow() {
        super("Визуализатор алгоритма Форда–Беллмана");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildToolBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 720);
        setLocationRelativeTo(null);
        updateControls();
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

        intervalField = new JTextField(String.valueOf(DEFAULT_INTERVAL_MS), 5);
        intervalField.setMaximumSize(new Dimension(60, 28));
        intervalField.setToolTipText("Интервал автоматического режима, мс");
        JLabel intervalLabel = new JLabel("Интервал, мс:");

        editButton.setToolTipText("Режим редактирования: клик по свободному месту — новая вершина, "
                + "клик по вершине — редактирование");

        loadButton.addActionListener(e -> chooseAndLoad());
        editButton.addActionListener(e -> toggleEditMode());
        runButton.addActionListener(e -> runAlgorithm());
        prevButton.addActionListener(e -> stepBackward());
        nextButton.addActionListener(e -> stepForward());
        saveButton.addActionListener(e -> chooseAndSave());
        aboutButton.addActionListener(e -> AboutDialog.show(this));
        autoButton.addActionListener(e -> toggleAuto());

        // Интервал применяется на лету, если авто-режим уже идёт.
        intervalField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyInterval(); }
            @Override public void removeUpdate(DocumentEvent e) { applyInterval(); }
            @Override public void changedUpdate(DocumentEvent e) { applyInterval(); }
        });

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

    private JPanel buildCenter() {
        centerHolder = new JPanel(new BorderLayout());
        split = new MainSplitPane(new Graph()); // пустой граф до загрузки
        configureGraphEditing();
        centerHolder.add(split, BorderLayout.CENTER);
        return centerHolder;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());

        explanationArea = new JTextArea(6, 20);
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        explanationArea.setText("Нажмите «Загрузить файл», затем «Запустить». "
                + "Шаги проходятся кнопками «Назад»/«Вперёд» или в авто-режиме.");

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setBorder(BorderFactory.createTitledBorder("Пояснение шага"));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        passLabel = new JLabel("Проход: —");
        stepLabel = new JLabel("Шаг: —");
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

    // ---------- Загрузка ----------

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
     * Загружает граф из файла и показывает его. Ошибки файла выводятся через
     * модальное окно и не роняют приложение.
     */
    public void loadGraph(Path path) {
        try {
            Graph graph = GraphParser.parse(path);
            stopAuto();
            currentGraph = graph;
            history = null;

            split = new MainSplitPane(graph);
            configureGraphEditing();
            centerHolder.removeAll();
            centerHolder.add(split, BorderLayout.CENTER);
            centerHolder.revalidate();
            centerHolder.repaint();
            split.getGraphPanel().updateSize();

            explanationArea.setText("Граф загружен: вершин — " + graph.getVertexCount()
                    + ", рёбер — " + graph.getEdgeCount()
                    + ", источник — " + graph.getSource().getName() + ".\n"
                    + "Нажмите «Запустить».");
            passLabel.setText("Проход: —");
            stepLabel.setText("Шаг: —");
            updateControls();
        } catch (GraphParseException ex) {
            MessageDialog.show(this, "Ошибка загрузки файла", ex.getMessage());
        }
    }

    // ---------- Запуск и навигация ----------

    /** Строит историю шагов и показывает начальное состояние алгоритма. */
    public void runAlgorithm() {
        if (currentGraph == null) {
            MessageDialog.show(this, "Граф не загружен", "Сначала загрузите файл с графом.");
            return;
        }
        if (currentGraph.getVertexCount() == 0) {
            MessageDialog.show(this, "Граф пуст", "Добавьте хотя бы одну вершину или загрузите файл с графом.");
            return;
        }
        if (currentGraph.getSource() == null) {
            MessageDialog.show(this, "Источник не задан",
                    "Укажите стартовую вершину командой SOURCE в окне редактирования вершины.");
            return;
        }
        try {
            stopAuto();
            history = StepHistory.fromGraph(currentGraph);
            if (history.isEmpty()) {
                MessageDialog.show(this, "Нет шагов", "Не удалось построить шаги алгоритма.");
                history = null;
                return;
            }
            showStep(); // начальное состояние (шаг инициализации)
            updateControls();
            // Отрицательный цикл не показываем всплывающим окном: он наглядно
            // виден при проходе шагов (дополнительный проход, где расстояния
            // продолжают уменьшаться) и в сохранённом файле результата.
        } catch (RuntimeException ex) {
            MessageDialog.show(this, "Ошибка алгоритма",
                    ex.getMessage() != null ? ex.getMessage() : "Не удалось выполнить алгоритм.");
            history = null;
            updateControls();
        }
    }

    /** Переход к следующему шагу. */
    public void stepForward() {
        if (history != null && history.hasNext()) {
            history.next();
            showStep();
            updateControls();
        }
    }

    /** Переход к предыдущему шагу. */
    public void stepBackward() {
        if (history != null && history.hasPrevious()) {
            history.previous();
            showStep();
            updateControls();
        }
    }

    /** Отрисовывает текущий шаг: граф, таблица, пояснение, номера. */
    private void showStep() {
        StepState step = history.current();
        split.updateStep(step);
        if (step != null) {
            explanationArea.setText(step.getExplanation());
            explanationArea.setCaretPosition(0);
            passLabel.setText("Проход: " + step.getPassNumber());
            stepLabel.setText("Шаг: " + step.getStepNumber() + " из " + (history.size() - 1));
        }
    }

    // ---------- Авто-режим ----------

    private void toggleAuto() {
        if (autoButton.isSelected()) {
            startAuto();
        } else {
            stopAuto();
        }
    }

    private void startAuto() {
        if (history == null || !history.hasNext()) {
            autoButton.setSelected(false);
            return;
        }
        int interval;
        try {
            interval = readInterval();
        } catch (IllegalArgumentException ex) {
            autoButton.setSelected(false);
            MessageDialog.show(this, "Ошибка интервала", ex.getMessage());
            return;
        }

        autoTimer = new Timer(interval, e -> {
            if (history != null && history.hasNext()) {
                history.next();
                showStep();
                if (!history.hasNext()) {
                    stopAuto();
                }
            } else {
                stopAuto();
            }
        });
        autoTimer.start();
        updateControls();
    }

    private void stopAuto() {
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
        autoButton.setSelected(false);
        updateControls();
    }

    private void applyInterval() {
        if (autoTimer != null && autoTimer.isRunning()) {
            try {
                autoTimer.setDelay(readInterval());
            } catch (IllegalArgumentException ex) {
                stopAuto();
                MessageDialog.show(this, "Ошибка интервала", ex.getMessage());
            }
        }
    }

    private int readInterval() {
        String text = intervalField.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Интервал автоматического режима не указан.");
        }

        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Интервал должен быть целым числом миллисекунд.");
        }

        if (value < MIN_INTERVAL_MS) {
            throw new IllegalArgumentException(
                    "Интервал должен быть не меньше " + MIN_INTERVAL_MS + " мс."
            );
        }

        return value;
    }

    // ---------- Сохранение ----------

    private void chooseAndSave() {
        if (history == null) {
            MessageDialog.show(this, "Нет результата", "Сначала запустите алгоритм («Запустить»).");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить результат");
        chooser.setSelectedFile(new File("result.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            if (Files.exists(path) && !confirmOverwrite(path)) {
                return;
            }
            try {
                AlgorithmResultExporter.save(currentGraph, history, path);
                JOptionPane.showMessageDialog(this, "Результат сохранён:\n" + path,
                        "Сохранено", JOptionPane.INFORMATION_MESSAGE);
            } catch (ResultOutputException ex) {
                MessageDialog.show(this, "Ошибка сохранения", ex.getMessage());
            }
        }
    }

    private void configureGraphEditing() {
        split.setGraphChangedHandler(this::handleGraphEdited);
        split.setEditMode(editButton != null && editButton.isSelected());
    }

    /** Включает или выключает режим ручного редактирования графа. */
    private void toggleEditMode() {
        boolean editing = editButton.isSelected();
        if (editing) {
            stopAuto();
        }
        split.setEditMode(editing);
        if (editing) {
            explanationArea.setText("Режим редактирования включён.\n"
                    + "Клик по свободному месту — создать вершину, клик по вершине — редактировать её.\n"
                    + "В окне вершины: команда «EDGE A B 10» добавляет ребро, «SOURCE» делает вершину стартовой.");
        }
        updateControls();
    }

    private void handleGraphEdited() {
        stopAuto();
        currentGraph = split.getGraph();
        history = null;
        split.resetAlgorithmView();
        split.refreshGraph();
        explanationArea.setText("Граф изменён. Нажмите «Запустить», чтобы заново построить шаги алгоритма.");
        passLabel.setText("Проход: —");
        stepLabel.setText("Шаг: —");
        updateControls();
    }

    private boolean confirmOverwrite(Path path) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Файл уже существует:\n" + path + "\n\nПерезаписать его?",
                "Подтверждение сохранения",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    // ---------- Состояние кнопок ----------

    /** Включает/выключает кнопки в зависимости от текущего состояния. */
    private void updateControls() {
        boolean graphLoaded = currentGraph != null;
        boolean hasHistory = history != null;
        boolean autoRunning = autoTimer != null && autoTimer.isRunning();

        runButton.setEnabled(graphLoaded && !autoRunning);
        saveButton.setEnabled(hasHistory && !autoRunning);
        loadButton.setEnabled(!autoRunning);
        autoButton.setEnabled(hasHistory && (history.hasNext() || autoRunning));
        prevButton.setEnabled(hasHistory && history.hasPrevious() && !autoRunning);
        nextButton.setEnabled(hasHistory && history.hasNext() && !autoRunning);
        editButton.setEnabled(!autoRunning);
    }
}
