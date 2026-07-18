package visualizer.parser;

public final class EditParser {
    private static final String EDGE = "EDGE";
    private static final String SOURCE = "SOURCE";

    private EditParser() {
    }

    public static EditCommand parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Команда не указана.");
        }

        String[] tokens = text.trim().split("\\s+");
        return switch (tokens[0]) {
            case EDGE -> parseEdge(tokens);
            case SOURCE -> parseSource(tokens);
            default -> throw new IllegalArgumentException("Неизвестная команда: " + tokens[0] + ".");
        };
    }

    private static EditCommand parseEdge(String[] tokens) {
        if (tokens.length != 4) {
            throw new IllegalArgumentException("Команда EDGE должна иметь формат: EDGE A B 100.");
        }

        int weight;
        try {
            weight = Integer.parseInt(tokens[3]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Вес ребра должен быть целым числом.");
        }

        return new EditCommand(EditCommand.Type.EDGE, tokens[1], tokens[2], weight);
    }

    private static EditCommand parseSource(String[] tokens) {
        if (tokens.length != 1) {
            throw new IllegalArgumentException("Команда SOURCE должна вводиться без аргументов.");
        }

        return new EditCommand(EditCommand.Type.SOURCE, null, null, 0);
    }
}
