package visualizer.model;

/**
 * Одна вершина графа с именем и координатами для отображения.
 */
public class Vertex {
    private String name;
    private int x;
    private int y;

    Vertex(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает уникальное имя вершины.
     *
     * @return имя вершины
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает координату X на холсте.
     *
     * @return координата X
     */
    public int getX() {
        return x;
    }

    /**
     * Устанавливает координату X на холсте.
     *
     * @param x координата X
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Возвращает координату Y на холсте.
     *
     * @return координата Y
     */
    public int getY() {
        return y;
    }

    /**
     * Устанавливает координату Y на холсте.
     *
     * @param y координата Y
     */
    public void setY(int y) {
        this.y = y;
    }

    void rename(String name) {
        this.name = name;
    }
}
