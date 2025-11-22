package academy.format.factory;

import academy.enums.ReportFileType;
import academy.format.AsciiDocFormatter;
import academy.format.JsonFormatter;
import academy.format.MarkdownFormatter;
import academy.format.ReportFormatter;

public class FormatterFactory {
    public static ReportFormatter createFormatter(ReportFileType type) {
        return switch (type) {
            case JSON -> new JsonFormatter();
            case MARKDOWN -> new MarkdownFormatter();
            case ADOC -> new AsciiDocFormatter();
        };
    }
}
