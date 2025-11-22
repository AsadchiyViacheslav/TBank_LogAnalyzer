package academy.stats;

import academy.enums.WeekDay;
import academy.input.LogSource;
import academy.model.DailyRequestStat;
import academy.model.LogAnalysisResult;
import academy.model.NginxLogEntry;
import academy.model.ResourceStat;
import academy.model.ResponseCodeStat;
import academy.model.ResponseSizeStats;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogStatisticsCollector {
    private static final Logger logger = LogManager.getLogger(LogStatisticsCollector.class);

    private final LocalDate fromDate;
    private final LocalDate toDate;

    private long totalRequests = 0;
    private final List<Long> responseSizes = new ArrayList<>();
    private final Map<Integer, Long> statusCodeCounts = new HashMap<>();
    private final Map<String, Long> resourceCounts = new HashMap<>();
    private final Map<LocalDate, Long> dailyRequestCounts = new TreeMap<>();
    private final Set<String> protocols = new HashSet<>();

    public LogStatisticsCollector(LocalDate fromDate, LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        logger.info("Создан LogStatisticsCollector для диапазона дат: {} до {}", fromDate, toDate);
    }

    public void addEntry(NginxLogEntry entry) {
        LocalDate entryDate = entry.timestamp().toLocalDate();

        if (!isInDateRange(entryDate)) {
            return;
        }

        totalRequests++;
        responseSizes.add(entry.bodyBytes());
        statusCodeCounts.merge(entry.statusCode(), 1L, Long::sum);
        resourceCounts.merge(entry.resource(), 1L, Long::sum);
        dailyRequestCounts.merge(entryDate, 1L, Long::sum);
        protocols.add(entry.protocol());
    }

    private boolean isInDateRange(LocalDate date) {
        if (fromDate != null && date.isBefore(fromDate)) return false;
        return toDate == null || !date.isAfter(toDate);
    }

    public LogAnalysisResult buildResult(List<LogSource> sources) {
        List<String> fileNames = sources.stream().map(LogSource::getDescription).collect(Collectors.toList());

        ResponseSizeStats sizeStats = calculateResponseSizeStats();
        List<ResourceStat> topResources = getTopResources();
        List<ResponseCodeStat> responseCodeStats = getResponseCodeStats();
        List<DailyRequestStat> dailyStats = getDailyRequestStats();
        List<String> uniqueProtocols = protocols.stream().sorted().collect(Collectors.toList());

        logger.info("Завершен сбор статистики: общее количество запросов={}", totalRequests);

        return new LogAnalysisResult(
                fileNames, totalRequests, sizeStats, topResources, responseCodeStats, dailyStats, uniqueProtocols);
    }

    private ResponseSizeStats calculateResponseSizeStats() {
        if (responseSizes.isEmpty()) {
            logger.warn("Размеры ответов не зафиксированы");
            return new ResponseSizeStats(0, 0, 0);
        }

        Collections.sort(responseSizes);
        double average =
                responseSizes.stream().mapToLong(Long::longValue).average().orElse(0);

        long max = responseSizes.getLast();
        double p95 = calculatePercentile(95);

        logger.debug("Рассчитанная статистика по размеру ответов: average={}, max={}, p95={}", average, max, p95);

        return new ResponseSizeStats(round(average), (double) max, round(p95));
    }

    private double calculatePercentile(int percentile) {
        if (responseSizes.isEmpty()) return 0;

        int n = responseSizes.size();
        double p = percentile / 100.0;
        double position = p * (n - 1);

        int index = (int) position;
        double fraction = position - index;

        if (fraction == 0) {
            return responseSizes.get(index);
        }

        double lower = responseSizes.get(index);
        double upper = responseSizes.get(index + 1);
        return lower + fraction * (upper - lower);
    }

    private List<ResourceStat> getTopResources() {
        return resourceCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> new ResourceStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ResponseCodeStat> getResponseCodeStats() {
        return statusCodeCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getKey(), a.getKey()))
                .map(e -> new ResponseCodeStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<DailyRequestStat> getDailyRequestStats() {
        return dailyRequestCounts.entrySet().stream()
                .map(e -> {
                    LocalDate date = e.getKey();
                    long count = e.getValue();
                    double percentage = round((count * 100.0) / totalRequests);
                    String dayName = getDayName(date.getDayOfWeek());

                    return new DailyRequestStat(date.toString(), dayName, count, percentage);
                })
                .collect(Collectors.toList());
    }

    private String getDayName(DayOfWeek dow) {
        return WeekDay.fromJavaDay(dow).getDisplayName();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
