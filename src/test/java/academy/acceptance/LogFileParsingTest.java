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

public class LogFileParsingTest {

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
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute("-p", logFile.getAbsolutePath(), "-f", "json", "-o", outputPath);

        assertThat(exitCode).isEqualTo(0);
        assertThat(Files.exists(outputFilePath)).isTrue();
        String content = Files.readString(outputFilePath);
        assertThat(content).contains("totalRequestsCount");
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() throws IOException {
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
                "-p",
                        "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs",
                "-f", "json",
                "-o", outputPath);

        assertThat(exitCode).isEqualTo(0);
        assertThat(Files.exists(outputFilePath)).isTrue();
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл, "
            + "часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
                "-p", logFile.getAbsolutePath(),
                "-f", "json",
                "-o", outputPath,
                "--from", "2015-05-22",
                "--to", "2015-05-23");

        assertThat(exitCode).isEqualTo(0);
        String content = Files.readString(outputFilePath);
        assertThat(content).contains("\"totalRequestsCount\": 2");
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() throws IOException {
        String mixedContent =
                String.join("\n", TestUtils.TEST_LOG_LINES[0], "invalid log line", TestUtils.TEST_LOG_LINES[1]);
        logFile = File.createTempFile("test-", ".log");
        Files.write(logFile.toPath(), mixedContent.getBytes());

        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute("-p", logFile.getAbsolutePath(), "-f", "json", "-o", outputPath);

        assertThat(exitCode).isEqualTo(0);
        String content = Files.readString(outputFilePath);
        assertThat(content).contains("totalRequestsCount");
    }
}
