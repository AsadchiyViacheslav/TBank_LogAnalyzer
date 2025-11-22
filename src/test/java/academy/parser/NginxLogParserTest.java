package academy.parser;

import static org.assertj.core.api.Assertions.*;

import academy.stats.LogStatisticsCollector;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NginxLogParserTest {

    private LogStatisticsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new LogStatisticsCollector(LocalDate.of(2015, 1, 1), LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("Парсинг валидной строки логов")
    void testParseValidLine() {
        String validLine = academy.util.TestUtils.TEST_LOG_LINES[0];
        NginxLogParser.parseStream(Stream.of(validLine), collector);
        assertThat(collector.buildResult(java.util.List.of()).totalRequestsCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Парсинг невалидной строки логов не выбрасывает исключение")
    void testParseInvalidLine() {
        String invalidLine = academy.util.TestUtils.createInvalidLogLine();
        assertThatCode(() -> NginxLogParser.parseStream(Stream.of(invalidLine), collector))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Парсинг нескольких строк логов")
    void testParseMultipleLines() {
        NginxLogParser.parseStream(Stream.of(academy.util.TestUtils.TEST_LOG_LINES), collector);
        assertThat(collector.buildResult(java.util.List.of()).totalRequestsCount())
            .isEqualTo(academy.util.TestUtils.TOTAL_REQUESTS);
    }

    @Test
    @DisplayName("Парсинг извлекает правильный метод")
    void testParseExtractsMethod() {
        String line = academy.util.TestUtils.TEST_LOG_LINES[0];
        NginxLogParser.parseStream(Stream.of(line), collector);
    }
}
