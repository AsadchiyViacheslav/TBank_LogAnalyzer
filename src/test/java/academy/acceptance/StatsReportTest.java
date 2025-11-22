package academy.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import academy.util.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class StatsReportTest {

    private File logFile;
    private String outputPath;
    private Path outputFilePath;
    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        cmd = new CommandLine(new academy.cli.LogAnalyzerCommand());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (logFile != null && logFile.exists()) {
            Files.deleteIfExists(logFile.toPath());
        }

        if (outputFilePath != null) {
            Files.deleteIfExists(outputFilePath);
        }
    }

    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest() throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute("-p", logFile.getAbsolutePath(), "-f", "json", "-o", outputPath);

        assertThat(exitCode).isEqualTo(0);
        String content = Files.readString(outputFilePath);
        assertThat(content).contains("{").contains("}").contains("totalRequestsCount");
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("md");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute("-p", logFile.getAbsolutePath(), "-f", "markdown", "-o", outputPath);

        assertThat(exitCode).isEqualTo(0);
        String content = Files.readString(outputFilePath);
        assertThat(content).contains("#").contains("|").contains("Анализ логов");
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("adoc");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute("-p", logFile.getAbsolutePath(), "-f", "adoc", "-o", outputPath);

        assertThat(exitCode).isEqualTo(0);
        String content = Files.readString(outputFilePath);
        assertThat(content).contains("=").contains("|").contains("Анализ логов");
    }
}
