package visualizer.parser;

public record EditCommand(Type type, String fromName, String toName, int weight) {
    public enum Type {
        EDGE,
        SOURCE
    }
}
