package academy.parser;

import academy.model.NginxLogEntry;
import academy.stats.LogStatisticsCollector;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NginxLogParser {
    private static final Logger logger = LogManager.getLogger(NginxLogParser.class);

    // IP-адрес клиента
    private static final String IP = "([\\d.]+)";

    // Идентификатор клиента, если нет то '-')
    private static final String DASH = "-";

    // Идентификатор пользователя
    private static final String CLIENT_ID = "([^ ]*)";

    // Дата и время запроса в квадратных скобках
    private static final String TIMESTAMP = "\\[([^]]+)]";

    // HTTP-метод
    private static final String METHOD = "([A-Z]+)";

    // Запрашиваемый ресурс
    private static final String RESOURCE = "([^ ]+)";

    // Протокол запроса
    private static final String PROTOCOL = "([^ ]+)";

    // HTTP-код ответа
    private static final String STATUS = "(\\d+)";

    // Размер тела ответа в байтах
    private static final String BYTES = "(\\d+)";

    // Referer
    private static final String REFERRER = "\"[^\"]*\"";

    // User-Agent
    private static final String USER_AGENT = "\"[^\"]*\"";

    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^" +
            IP + " " +
            DASH + " " +
            CLIENT_ID + " " +
            TIMESTAMP + " " +
            "\"" + METHOD + " " +
            RESOURCE + " " +
            PROTOCOL + "\"" + " " +
            STATUS + " " +
            BYTES + " " +
            REFERRER + " " +
            USER_AGENT +
            "$"
    );

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", java.util.Locale.ENGLISH);

    public static void parseStream(Stream<String> lines, LogStatisticsCollector collector) {
        lines.forEach(line -> parseLine(line, collector));
    }

    private static void parseLine(String line, LogStatisticsCollector collector) {
        try {
            Matcher matcher = LOG_PATTERN.matcher(line);
            if (!matcher.matches()) {
                logger.warn("Ошибка парсинга строки: {}", line);
                return;
            }

            String remoteAddr = matcher.group(1);
            NginxLogEntry entry = getNginxLogEntry(matcher, remoteAddr);

            collector.addEntry(entry);
        } catch (Exception e) {
            logger.warn("Ошибка парсинга строки: {}", line, e);
        }
    }

    private static NginxLogEntry getNginxLogEntry(Matcher matcher, String remoteAddr) {
        String dateStr = matcher.group(3);
        String method = matcher.group(4);
        String resource = matcher.group(5);
        String protocol = matcher.group(6);
        int statusCode = Integer.parseInt(matcher.group(7));
        long bodyBytes = Long.parseLong(matcher.group(8));

        LocalDateTime timestamp = LocalDateTime.parse(dateStr, DATE_FORMATTER);

        return new NginxLogEntry(remoteAddr, timestamp, method, resource, protocol, statusCode, bodyBytes);
    }
}
