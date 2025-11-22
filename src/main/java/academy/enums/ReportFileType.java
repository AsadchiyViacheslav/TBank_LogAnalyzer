package academy.enums;

import lombok.Getter;

@Getter
public enum ReportFileType {
    JSON("json"),
    MARKDOWN("md"),
    ADOC("adoc");

    private final String extension;

    ReportFileType(String extension) {
        this.extension = extension;
    }

    public static ReportFileType fromString(String name) {
        return switch (name.toLowerCase()) {
            case "json" -> JSON;
            case "markdown" -> MARKDOWN;
            case "adoc" -> ADOC;
            default -> throw new IllegalArgumentException("Неподдерживаемый формат: " + name);
        };
    }
}
