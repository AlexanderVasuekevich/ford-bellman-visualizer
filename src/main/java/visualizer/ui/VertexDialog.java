package visualizer.ui;

import visualizer.model.Edge;
import visualizer.model.Graph;
import visualizer.model.Vertex;
import visualizer.parser.EditCommand;
import visualizer.parser.EditParser;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VertexDialog extends JDialog {
    private final Graph graph;
    private final Graph draftGraph;
    private final Runnable graphChangedHandler;
    private final boolean newVertex;

    private String draftVertexName;
    private String pendingName;
    private boolean suppressNameEvents;
    private boolean saved;

    private final JTextField nameField = new JTextField();
    private final JTextField commandField = new JTextField();
    private final DefaultListModel<Edge> edgeListModel = new DefaultListModel<>();
    private final JList<Edge> edgeList = new JList<>(edgeListModel);

    public VertexDialog(Window parent, Graph graph, Vertex vertex, int x, int y, Runnable graphChangedHandler) {
        super(parent, "Редактирование вершины", ModalityType.APPLICATION_MODAL);
        this.graph = graph;
        this.draftGraph = copyGraph(graph);
        this.graphChangedHandler = graphChangedHandler;
        this.newVertex = vertex == null;

        if (newVertex) {
            draftVertexName = generateVertexName(draftGraph);
            draftGraph.addVertex(draftVertexName, x, y);
        } else {
            draftVertexName = vertex.getName();
        }

        pendingName = draftVertexName;
        buildBaseContent();
        bindHandlers();
        refreshFields();

        setSize(420, 360);
        setLocationRelativeTo(parent);
    }

    public VertexDialog(Frame parent) {
        this(parent, new Graph(), null, 0, 0, null);
    }

    public boolean isSaved() {
        return saved;
    }

    public boolean isConfirmed() {
        return saved;
    }

    public String getVertexName() {
        return pendingName == null ? "" : pendingName.trim();
    }

    public String getVertexX() {
        Vertex vertex = draftGraph.getVertex(draftVertexName);
        return vertex == null ? "" : String.valueOf(vertex.getX());
    }

    public String getVertexY() {
        Vertex vertex = draftGraph.getVertex(draftVertexName);
        return vertex == null ? "" : String.valueOf(vertex.getY());
    }

    public String getCommandText() {
        return commandField.getText().trim();
    }

    public void handleNameChanged() {
        if (!suppressNameEvents) {
            pendingName = nameField.getText().trim();
        }
    }

    public void handleCommandEnter() {
        try {
            applyPendingNameToDraft();
            EditCommand command = EditParser.parse(commandField.getText());
            switch (command.type()) {
                case EDGE -> draftGraph.addEdge(command.fromName(), command.toName(), command.weight());
                case SOURCE -> draftGraph.setSource(draftVertexName);
            }
            commandField.setText("");
            refreshEdgeList();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    public void handleDeleteSelectedEdge() {
        Edge selected = edgeList.getSelectedValue();
        if (selected == null) {
            return;
        }

        draftGraph.removeEdge(selected);
        refreshEdgeList();
    }

    public void handleDeleteVertex() {
        if (newVertex) {
            handleClose();
            return;
        }

        try {
            draftGraph.removeVertex(draftVertexName);
            graph.replaceWith(draftGraph);
            saved = true;
            notifyGraphChanged();
            dispose();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    public void handleSave() {
        try {
            applyPendingNameToDraft();
            graph.replaceWith(draftGraph);
            saved = true;
            notifyGraphChanged();
            dispose();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    public void handleClose() {
        saved = false;
        dispose();
    }

    private void buildBaseContent() {
        setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridLayout(2, 2, 6, 6));
        fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        fields.add(new JLabel("Имя вершины:"));
        fields.add(nameField);
        fields.add(new JLabel("Команда:"));
        fields.add(commandField);

        edgeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        edgeList.setCellRenderer((list, edge, index, selected, focused) -> {
            JLabel label = new JLabel(formatEdge(edge));
            label.setOpaque(true);
            label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
            return label;
        });

        JScrollPane edgesScroll = new JScrollPane(edgeList);
        edgesScroll.setBorder(BorderFactory.createTitledBorder("Связанные ребра"));

        add(fields, BorderLayout.NORTH);
        add(edgesScroll, BorderLayout.CENTER);
    }

    private void bindHandlers() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleNameChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { handleNameChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { handleNameChanged(); }
        });

        commandField.addActionListener(e -> handleCommandEnter());

        JPopupMenu edgeMenu = new JPopupMenu();
        JMenuItem deleteEdgeItem = new JMenuItem("Удалить ребро");
        deleteEdgeItem.addActionListener(e -> handleDeleteSelectedEdge());
        edgeMenu.add(deleteEdgeItem);

        edgeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = edgeList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        edgeList.setSelectedIndex(index);
                        edgeMenu.show(edgeList, e.getX(), e.getY());
                    }
                }
            }
        });

        getRootPane().registerKeyboardAction(
                e -> handleSave(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );
        getRootPane().registerKeyboardAction(
                e -> handleDeleteVertex(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );
        getRootPane().registerKeyboardAction(
                e -> handleClose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void refreshFields() {
        suppressNameEvents = true;
        nameField.setText(draftVertexName);
        suppressNameEvents = false;
        pendingName = draftVertexName;
        refreshEdgeList();
    }

    private void refreshEdgeList() {
        edgeListModel.clear();
        if (!draftGraph.hasVertex(draftVertexName)) {
            return;
        }
        for (Edge edge : draftGraph.getIncidentEdges(draftVertexName)) {
            edgeListModel.addElement(edge);
        }
    }

    private void applyPendingNameToDraft() {
        String normalizedName = pendingName == null ? "" : pendingName.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Имя вершины не может быть пустым.");
        }
        if (!normalizedName.equals(draftVertexName)) {
            draftGraph.renameVertex(draftVertexName, normalizedName);
            draftVertexName = normalizedName;
            refreshFields();
        }
    }

    private void notifyGraphChanged() {
        if (graphChangedHandler != null) {
            graphChangedHandler.run();
        }
    }

    private void showError(String message) {
        Component parent = getOwner() == null ? this : getOwner();
        MessageDialog.show(parent, "Ошибка редактирования графа",
                message == null || message.isBlank() ? "Не удалось выполнить действие." : message);
    }

    private static Graph copyGraph(Graph source) {
        Graph copy = new Graph();
        copy.replaceWith(source);
        return copy;
    }

    private static String generateVertexName(Graph graph) {
        int index = graph.getVertexCount() + 1;
        String name = "V" + index;
        while (graph.hasVertex(name)) {
            index++;
            name = "V" + index;
        }
        return name;
    }

    private static String formatEdge(Edge edge) {
        return "(" + edge.getFrom().getName()
                + "," + edge.getTo().getName()
                + "," + edge.getWeight() + ")";
    }
}
