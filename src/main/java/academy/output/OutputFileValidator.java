package academy.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputFileValidator {
    private static final String DOT = ".";
    private static final int EXTENSION_OFFSET = 1;

    public static void validate(String output, String format) throws IOException {
        Path outputPath = Path.of(output);

        Path parent = outputPath.getParent();
        if (parent != null && !Files.isWritable(parent)) {
            throw new IllegalArgumentException("Выходной каталог недоступен для записи: " + parent);
        }

        if (Files.exists(outputPath)) {
            throw new IllegalArgumentException("Выходной файл уже существует: " + output);
        }

        String ext = getFileExtension(output);
        String expectedExt = switch (format) {
            case "json" -> "json";
            case "markdown" -> "md";
            case "adoc" -> "adoc";
            default -> "";
        };

        if (!ext.equals(expectedExt)) {
            throw new IllegalArgumentException(
                "Расширение выходного файла должно быть ." + expectedExt + " для формата " + format
            );
        }
    }

    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(DOT);
        return lastDot > 0 ? filename.substring(lastDot + EXTENSION_OFFSET) : "";
    }
}
