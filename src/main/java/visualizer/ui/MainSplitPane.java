package visualizer.ui;

import visualizer.model.Graph;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import java.awt.Dimension;

public class MainSplitPane extends JSplitPane {
    
    private GraphPanel graphPanel;
    private DistanceTable distanceTable;

    public MainSplitPane(Graph graph) {
        super(JSplitPane.HORIZONTAL_SPLIT);

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

        this.setLeftComponent(graphScroll);
        this.setRightComponent(tableScroll);
        this.setResizeWeight(1.0);
        this.setDividerLocation(820);
    }

    public void setGraph(Graph graph) {
        if (graphPanel != null) {
            graphPanel.setGraph(graph);
        }
        if (distanceTable != null) {
            distanceTable.refreshTable(null);
        }
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public DistanceTable getDistanceTable() {
        return distanceTable;
    }

    public void clearHighlight() {
        if (graphPanel != null) {
            graphPanel.clearHighlight();
        }
    }

    public void updateViewport() {
        revalidate();
        repaint();
    }

    public void updateStep(visualizer.algorithm.AlgorithmStep step) {
        if (step != null && graphPanel != null) {
            graphPanel.setCurrentProcessingEdge(step.getEdgeIndex());
            graphPanel.setDistances(step.getDistances());
            graphPanel.setPredecessors(step.getPredecessors());
            
            if (step.isRelaxed() && step.getToVertex() != null) {
                graphPanel.setUpdatedVertex(step.getToVertex());
            } else {
                graphPanel.setUpdatedVertex(null);
            }
            graphPanel.repaint();
        }
        
        if (distanceTable != null) {
            distanceTable.refreshTable(step);
        }
    }
}