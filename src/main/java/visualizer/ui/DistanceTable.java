package visualizer.ui;

import visualizer.algorithm.BellmanFord;
import visualizer.algorithm.StepState;
import visualizer.model.Graph;
import visualizer.model.Vertex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Map;

/**
 * Таблица расстояний для визуализации алгоритма Форда-Беллмана
 * Столбцы: Vertex, Distance, Parent
 */
public class DistanceTable extends JTable {
    private static final String[] columns = {"Vertex", "Distance", "Parent"};
    private final Graph graph;
    private DefaultTableModel model;

    public DistanceTable(Graph graph) {
        this.graph = graph;
        initModel();
    }

    private void initModel() {
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // таблица только для просмотра
            }
        };
        setModel(model);
        // Заполняем начальными данными
        refreshTable(null);
    }

    /**
     * Обновление таблицы на основе шага алгоритма
     * @param step шаг алгоритма (если null - начальное состояние)
     */
    public void refreshTable(StepState step) {
        model.setRowCount(0); // Очищаем таблицу
        
        for (Vertex v : graph.getVertices()) {
            String name = v.getName();
            String distStr;
            String parentStr;
            
            if (step != null) {
                Map<String, Integer> distances = step.getDistances();
                Map<String, String> predecessors = step.getPredecessors();
                
                Integer dist = distances.get(name);
                distStr = (dist == null || dist == BellmanFord.INF) ? "INF" : String.valueOf(dist);
                parentStr = predecessors.get(name);
                if (parentStr == null) parentStr = "-";
            } else {
                // Начальное состояние (до запуска алгоритма)
                if (v == graph.getSource()) {
                    distStr = "0";
                    parentStr = "-";
                } else {
                    distStr = "INF";
                    parentStr = "-";
                }
            }
            
            model.addRow(new Object[]{name, distStr, parentStr});
        }
    }
    // Сброс к начальному состоянию
    public void resetTable() {
        refreshTable(null);
    }
}