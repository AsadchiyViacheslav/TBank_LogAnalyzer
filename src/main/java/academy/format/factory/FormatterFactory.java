package academy.format.factory;

import academy.format.AsciiDocFormatter;
import academy.format.JsonFormatter;
import academy.format.MarkdownFormatter;
import academy.format.ReportFormatter;

public class FormatterFactory {
    public static ReportFormatter createFormatter(String format) {
        return switch (format.toLowerCase()) {
            case "json" -> new JsonFormatter();
            case "markdown" -> new MarkdownFormatter();
            case "adoc" -> new AsciiDocFormatter();
            default -> throw new IllegalArgumentException("Неизвестный формат: " + format);
        };
    }
}
