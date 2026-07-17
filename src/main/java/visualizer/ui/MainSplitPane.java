package visualizer.ui;

import visualizer.algorithm.StepState;
import visualizer.model.Edge;
import visualizer.model.Graph;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import java.awt.Dimension;

/**
 * Центральная область: слева граф (GraphPanel), справа таблица расстояний
 * (DistanceTable), разделённые сплиттером.
 *
 * Автор структуры — Стрижков Иван. На Версии 2 переведён на StepState
 * (единый тип шага, StepHistory Бурменского) вместо AlgorithmStep.
 */
public class MainSplitPane extends JSplitPane {

    private final Graph graph;
    private final GraphPanel graphPanel;
    private final DistanceTable distanceTable;

    public MainSplitPane(Graph graph) {
        super(JSplitPane.HORIZONTAL_SPLIT);
        this.graph = graph;

        graphPanel = new GraphPanel(graph);
        JScrollPane graphScroll = new JScrollPane(
                graphPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        graphScroll.setBorder(BorderFactory.createTitledBorder("Граф"));

        distanceTable = new DistanceTable(graph);
        distanceTable.setRowHeight(24);
        distanceTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(
                distanceTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        tableScroll.setBorder(BorderFactory.createTitledBorder("Таблица расстояний"));
        tableScroll.setPreferredSize(new Dimension(260, 400));

        setLeftComponent(graphScroll);
        setRightComponent(tableScroll);
        setResizeWeight(1.0);
        setDividerLocation(820);
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public DistanceTable getDistanceTable() {
        return distanceTable;
    }

    public void clearHighlight() {
        graphPanel.clearHighlight();
    }

    /**
     * Обновляет граф и таблицу под текущий шаг алгоритма.
     * Подсвечивает текущее ребро, начальную/конечную/обновлённую вершины,
     * показывает расстояния и предшественников.
     *
     * @param step состояние шага (если {@code null} — начальное состояние)
     */
    public void updateStep(StepState step) {
        if (step != null) {
            Edge edge = step.getCurrentEdge();
            int edgeIndex = (edge == null) ? -1 : graph.getEdges().indexOf(edge);

            graphPanel.setCurrentProcessingEdge(edgeIndex);
            graphPanel.setDistances(step.getDistances());
            graphPanel.setPredecessors(step.getPredecessors());
            graphPanel.setUpdatedVertex(step.isUpdated() ? step.getUpdatedVertexName() : null);
            graphPanel.repaint();
        } else {
            graphPanel.clearHighlight();
        }
        distanceTable.refreshTable(step);
    }
}
