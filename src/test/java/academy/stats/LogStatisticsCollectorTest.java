package academy.stats;

import static org.assertj.core.api.Assertions.*;

import academy.input.LocalLogSource;
import academy.model.LogAnalysisResult;
import academy.parser.NginxLogParser;
import academy.util.TestUtils;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogStatisticsCollectorTest {

    private LogStatisticsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new LogStatisticsCollector(
            LocalDate.of(2015, 1, 1),
            LocalDate.of(2025, 12, 31)
        );
    }

    @Test
    @DisplayName("Расчет общего количества запросов")
    void testTotalRequestsCount() throws IOException {
        LogAnalysisResult result = parseDefault();
        assertThat(result.totalRequestsCount()).isEqualTo(TestUtils.TOTAL_REQUESTS);
    }

    @Test
    @DisplayName("Расчет среднего размера ответа")
    void testAverageResponseSize() throws IOException {
        LogAnalysisResult result = parseDefault();
        assertThat(result.responseSizeInBytes().average())
            .isCloseTo(TestUtils.AVERAGE_RESPONSE_SIZE, within(0.01));
    }

    @Test
    @DisplayName("Расчет максимального размера ответа")
    void testMaxResponseSize() throws IOException {
        LogAnalysisResult result = parseDefault();
        assertThat(result.responseSizeInBytes().max())
            .isEqualTo(TestUtils.MAX_RESPONSE_SIZE);
    }

    @Test
    @DisplayName("Расчет 95-го перцентиля")
    void testP95ResponseSize() throws IOException {
        LogAnalysisResult result = parseDefault();
        assertThat(result.responseSizeInBytes().p95())
            .isCloseTo(TestUtils.P95_RESPONSE_SIZE, within(0.01));
    }

    @Test
    @DisplayName("Количество уникальных ресурсов")
    void testUniqueResources() throws IOException {
        LogAnalysisResult result = parseDefault();
        assertThat(result.resources()).hasSize(TestUtils.UNIQUE_RESOURCES);
    }

    @Test
    @DisplayName("Количество кодов ответов")
    void testResponseCodes() throws IOException {
        LogAnalysisResult result = parseDefault();
        assertThat(result.responseCodes()).hasSize(TestUtils.UNIQUE_STATUS_CODES);
    }

    @Test
    @DisplayName("Фильтрация по диапазону дат")
    void testDateRangeFiltering() throws IOException {
        LogStatisticsCollector filteredCollector = new LogStatisticsCollector(
            LocalDate.of(2015, 5, 23),
            LocalDate.of(2015, 5, 23)
        );

        LogAnalysisResult result = parseAndBuildResult(filteredCollector);
        assertThat(result.totalRequestsCount()).isEqualTo(2);
    }

    private LogAnalysisResult parseAndBuildResult(LogStatisticsCollector col) throws IOException {
        File logFile = TestUtils.createTempLogFile();

        try (var stream = new LocalLogSource(logFile.toPath()).getLineStream()) {
            NginxLogParser.parseStream(stream, col);
        }

        return col.buildResult(List.of());
    }

    private LogAnalysisResult parseDefault() throws IOException {
        return parseAndBuildResult(collector);
    }
}
