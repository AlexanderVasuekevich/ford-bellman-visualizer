# Модель данных

Документ описывает предварительную модель данных для визуализатора алгоритма
Форда-Беллмана. Это проектное описание для обсуждения на этапе прототипа.

Базовые классы:

- `Graph` - граф и операции над ним.
- `Vertex` - вершина графа.
- `Edge` - ориентированное взвешенное ребро.

## Graph

```java
class Graph {
    private final Map<String, Vertex> vertices;
    private final List<Edge> edges;
    private Vertex source;
}
```

- `vertices` - вершины графа, сгруппированные по имени.
- `edges` - список всех ориентированных ребер графа.
- `source` - стартовая вершина для запуска алгоритма.

`Map<String, Vertex>` выбран потому, что имя вершины считается уникальным
идентификатором. Это удобно при чтении строк вида `EDGE A B 10`: по имени
можно быстро найти объект вершины.

`List<Edge>` выбран потому, что алгоритм Форда-Беллмана на каждом проходе
последовательно перебирает все ребра графа.

Предполагаемые операции:

```java
class Graph {
    public void addVertex(String name, int x, int y) {}

    public void removeVertex(String name) {}

    public void renameVertex(String oldName, String newName) {}

    public void addEdge(String fromName, String toName, int weight) {}

    public void removeEdge(Edge edge) {}

    public Vertex getVertex(String name) {}

    public Collection<Vertex> getVertices() {}

    public List<Edge> getEdges() {}

    public Vertex getSource() {}

    public void setSource(String name) {}
}
```

- `addVertex` добавляет новую вершину с уникальным именем.
- `removeVertex` удаляет вершину и все связанные с ней ребра.
- `renameVertex` переименовывает вершину и обновляет ключ в `vertices`.
- `addEdge` добавляет ориентированное ребро между существующими вершинами.
- `removeEdge` удаляет выбранное ребро.
- `setSource` устанавливает стартовую вершину по имени.

Переименование вершины должно выполняться только через `Graph`, а не напрямую
через `Vertex`. Иначе можно изменить имя объекта, но оставить старый ключ в
`Map<String, Vertex>`.

Удаление вершины должно автоматически удалять все входящие и исходящие ребра,
связанные с этой вершиной.

## Vertex

```java
class Vertex {
    private String name;
    private int x;
    private int y;
}
```

- `name` - уникальное имя вершины.
- `x`, `y` - координаты вершины на холсте.

## Edge

```java
class Edge {
    private Vertex from;
    private Vertex to;
    private int weight;
}
```

- `from` - начальная вершина ребра.
- `to` - конечная вершина ребра.
- `weight` - вес ребра.
