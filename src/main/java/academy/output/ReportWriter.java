package academy.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportWriter {
    private static final Logger logger = LogManager.getLogger(ReportWriter.class);

    public static void write(String output, String content) throws IOException {
        Path outputPath = Path.of(output);

        Files.writeString(outputPath, content);
        logger.info("Записано {} байт в {}", content.length(), output);
    }
}
