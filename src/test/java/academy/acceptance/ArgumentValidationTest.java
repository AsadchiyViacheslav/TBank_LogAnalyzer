package academy.acceptance;

import static org.assertj.core.api.Assertions.*;

import academy.util.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import picocli.CommandLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ArgumentValidationTest {

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
    @DisplayName("На вход передан несуществующий локальный файл")
    void testNonExistentLocalFile() throws IOException {
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", "/nonexistent/file.log",
            "-f", "json",
            "-o", outputPath
        );

        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void testNonExistentRemoteFile() throws IOException {
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", "https://example.com/nonexistent.log",
            "-f", "json",
            "-o", outputPath
        );

        assertThat(exitCode).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {".docx"})
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void testUnsupportedFileFormat(String extension) throws IOException {
        File unsupportedFile = File.createTempFile("test-", extension);
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        try {
            int exitCode = cmd.execute(
                "-p", unsupportedFile.getAbsolutePath(),
                "-f", "json",
                "-o", outputPath
            );
            assertThat(exitCode).isEqualTo(2);
        } finally {
            Files.deleteIfExists(unsupportedFile.toPath());
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to")
    void testInvalidDateFormat(String dateStr) throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        String[] args = dateStr == null
            ? new String[]{"-p", logFile.getAbsolutePath(), "-f", "json", "-o", outputPath, "--from"}
            : new String[]{"-p", logFile.getAbsolutePath(), "-f", "json", "-o", outputPath, "--from", dateStr};

        int exitCode = cmd.execute(args);
        assertThat(exitCode).isIn(1, 2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt"})
    @DisplayName("Результаты запрошены в неподдерживаемом формате")
    void testUnsupportedOutputFormat(String format) throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", logFile.getAbsolutePath(),
            "-f", format,
            "-o", outputPath
        );
        assertThat(exitCode).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"markdown", "json", "adoc"})
    @DisplayName("По пути в аргументе --output указан файл с некорректным расширением")
    void testMismatchedOutputExtension(String format) throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath(format.equals("markdown") ? "txt" : "md");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", logFile.getAbsolutePath(),
            "-f", format,
            "-o", outputPath
        );
        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void testExistingOutputFile() throws IOException {
        logFile = TestUtils.createTempLogFile();
        File outputFile = File.createTempFile("output-", ".json");
        outputFilePath = outputFile.toPath();

        try {
            int exitCode = cmd.execute(
                "-p", logFile.getAbsolutePath(),
                "-f", "json",
                "-o", outputFile.getAbsolutePath()
            );
            assertThat(exitCode).isEqualTo(2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр")
    void testMissingRequiredParameter(String param) throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        String[] baseArgs = {"-p", logFile.getAbsolutePath(), "-f", "json", "-o", outputPath};
        java.util.List<String> args = new java.util.ArrayList<>(java.util.Arrays.asList(baseArgs));

        if (param.equals("--path") || param.equals("-p")) {
            args.remove(logFile.getAbsolutePath());
            args.remove("-p");
        } else if (param.equals("--format") || param.equals("-f")) {
            args.remove("json");
            args.remove("-f");
        } else if (param.equals("--output") || param.equals("-o")) {
            args.remove(outputPath);
            args.remove("-o");
        }

        int exitCode = cmd.execute(args.toArray(new String[0]));
        assertThat(exitCode).isNotEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("На вход передан неподдерживаемый параметр")
    void testUnsupportedParameter(String param) throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", logFile.getAbsolutePath(),
            "-f", "json",
            "-o", outputPath,
            param, "value"
        );
        assertThat(exitCode).isNotEqualTo(0);
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void testInvalidDateRange() throws IOException {
        logFile = TestUtils.createTempLogFile();
        outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", logFile.getAbsolutePath(),
            "-f", "json",
            "-o", outputPath,
            "--from", "2025-12-31",
            "--to", "2025-01-01"
        );
        assertThat(exitCode).isEqualTo(2);
    }
}
