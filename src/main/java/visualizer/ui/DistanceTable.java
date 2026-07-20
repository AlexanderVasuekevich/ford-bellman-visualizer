package visualizer.ui;

import visualizer.algorithm.BellmanFord;
import visualizer.algorithm.StepState;
import visualizer.model.Graph;
import visualizer.model.Vertex;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.Set;

/**
 * Таблица расстояний для визуализации алгоритма Форда-Беллмана
 * Столбцы: Vertex, Distance, Parent
 *
 * Строка вершины, расстояние до которой обновилось на текущем шаге,
 * подсвечивается зеленым. Расстояния до вершин, достижимых из
 * отрицательного цикла, выводятся в скобках.
 */
public class DistanceTable extends JTable {
    private static final String[] columns = {"Vertex", "Distance", "Parent"};
    private static final Color UPDATED_ROW_COLOR = new Color(200, 255, 200);

    private final Graph graph;
    private DefaultTableModel model;
    private String updatedVertexName; // Вершина, обновленная на текущем шаге

    public DistanceTable(Graph graph) {
        this.graph = graph;
        initModel();
        initRenderer();
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
     * Рендерер подсвечивает строку вершины, расстояние до которой
     * обновилось на текущем шаге алгоритма.
     */
    private void initRenderer() {
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column
            ) {
                Component cell = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String vertexName = String.valueOf(table.getModel().getValueAt(row, 0));
                    boolean highlighted = updatedVertexName != null
                            && updatedVertexName.equals(vertexName);
                    cell.setBackground(highlighted ? UPDATED_ROW_COLOR : table.getBackground());
                }
                return cell;
            }
        });
    }

    /**
     * Обновление таблицы на основе шага алгоритма
     * @param step шаг алгоритма (если null - начальное состояние)
     */
    public void refreshTable(StepState step) {
        model.setRowCount(0); // Очищаем таблицу
        updatedVertexName = (step != null && step.isUpdated())
                ? step.getUpdatedVertexName()
                : null;

        Set<String> negativeCycleAffected = step == null
                ? Set.of()
                : step.getNegativeCycleAffectedVertices();

        for (Vertex v : graph.getVertices()) {
            String name = v.getName();
            String distStr;
            String parentStr;

            if (step != null) {
                Map<String, Integer> distances = step.getDistances();
                Map<String, String> predecessors = step.getPredecessors();

                Integer dist = distances.get(name);
                distStr = (dist == null || dist == BellmanFord.INF) ? "INF" : String.valueOf(dist);
                if (negativeCycleAffected.contains(name)) {
                    // Расстояние не определено: вершина достижима из отрицательного цикла
                    distStr = "(" + distStr + ")";
                }
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
