package academy.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportWriter {
    private static final Logger logger = LogManager.getLogger(ReportWriter.class);

    public static void write(String output, String content, String format) throws IOException {
        Path outputPath = Path.of(output);

        if (Files.exists(outputPath)) {
            throw new IllegalArgumentException("Выходной файл уже существует: " + output);
        }
        Path parent = outputPath.getParent();
        if (parent != null && !Files.isWritable(parent)) {
            throw new IllegalArgumentException("Выходной каталог недоступен для записи: " + parent);
        }

        String ext = getFileExtension(output);
        String expectedExt =
                switch (format) {
                    case "json" -> "json";
                    case "markdown" -> "md";
                    case "adoc" -> "adoc";
                    default -> "";
                };

        if (!ext.equals(expectedExt)) {
            throw new IllegalArgumentException(
                    "Расширение выходного файла должно быть ." + expectedExt + " для формата " + format);
        }

        Files.writeString(outputPath, content);
        logger.info("Записано {} байт в {}", content.length(), output);
    }

    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
}
