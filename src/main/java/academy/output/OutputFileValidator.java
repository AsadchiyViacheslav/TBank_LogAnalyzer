package academy.output;

import academy.enums.ReportFileType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputFileValidator {
    private static final String DOT = ".";
    private static final int EXTENSION_OFFSET = 1;

    public static void validate(String output, ReportFileType expectedFileType) throws IOException {
        Path outputPath = Path.of(output);

        Path parent = outputPath.getParent();
        if (parent != null && !Files.isWritable(parent)) {
            throw new IllegalArgumentException("Выходной каталог недоступен для записи: " + parent);
        }

        String ext = getFileExtension(output);
        if (!ext.equalsIgnoreCase(expectedFileType.getExtension())) {
            throw new IllegalArgumentException(
                    "Расширение выходного файла должно быть ." + expectedFileType.getExtension() + " для формата "
                            + expectedFileType.name().toLowerCase());
        }

        if (Files.exists(outputPath)) {
            throw new IllegalArgumentException("Выходной файл уже существует: " + output);
        }
    }

    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(DOT);
        return lastDot > 0 ? filename.substring(lastDot + EXTENSION_OFFSET) : "";
    }
}
