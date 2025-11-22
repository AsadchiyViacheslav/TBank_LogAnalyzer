package academy.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import academy.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StatsCalculationTest {

    private File logFile;
    private Path outputFilePath;
    private CommandLine cmd;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        cmd = new CommandLine(new academy.cli.LogAnalyzerCommand());
        mapper = new ObjectMapper();
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
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() throws IOException {
        logFile = TestUtils.createTempLogFile();
        String outputPath = TestUtils.createOutputPath("json");
        outputFilePath = Path.of(outputPath);

        int exitCode = cmd.execute(
            "-p", logFile.getAbsolutePath(),
            "-f", "json",
            "-o", outputPath
        );

        assertThat(exitCode).isEqualTo(0);

        String content = Files.readString(outputFilePath);
        var jsonNode = mapper.readTree(content);

        assertThat(jsonNode.get("totalRequestsCount").asLong())
            .isEqualTo(TestUtils.TOTAL_REQUESTS);
        assertThat(jsonNode.get("responseSizeInBytes").get("average").asDouble())
            .isCloseTo(TestUtils.AVERAGE_RESPONSE_SIZE, org.assertj.core.api.Assertions.within(0.01));
        assertThat(jsonNode.get("responseSizeInBytes").get("max").asDouble())
            .isEqualTo(TestUtils.MAX_RESPONSE_SIZE);
        assertThat(jsonNode.get("responseSizeInBytes").get("p95").asDouble())
            .isEqualTo(TestUtils.P95_RESPONSE_SIZE, org.assertj.core.api.Assertions.within(0.01));
        assertThat(jsonNode.get("resources").size())
            .isEqualTo(TestUtils.UNIQUE_RESOURCES);
        assertThat(jsonNode.get("responseCodes").size())
            .isEqualTo(TestUtils.UNIQUE_STATUS_CODES);
    }
}
