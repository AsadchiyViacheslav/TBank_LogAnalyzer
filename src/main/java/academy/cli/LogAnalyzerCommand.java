package academy.cli;

import academy.format.ReportFormatter;
import academy.format.factory.FormatterFactory;
import academy.input.LogSourceProvider;
import academy.model.LogAnalysisResult;
import academy.output.OutputFileValidator;
import academy.output.ReportWriter;
import academy.parser.NginxLogParser;
import academy.stats.LogStatisticsCollector;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

@CommandLine.Command(
        name = "log-analyzer",
        version = "1.0.0",
        description = "Утилита для анализа логов NGINX",
        mixinStandardHelpOptions = true)
public class LogAnalyzerCommand implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);

    @CommandLine.Option(
            names = {"-p", "--path"},
            required = true,
            arity = "1..*",
            description = "Пути к файлам логов NGINX (поддерживает шаблоны глобальных объектов и URL-адреса)")
    private String[] paths;

    @CommandLine.Option(
            names = {"-f", "--format"},
            required = true,
            description = "Выходной формат: json, markdown, adoc")
    private String format;

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = true,
            description = "Путь к выходному файлу")
    private String output;

    @CommandLine.Option(
            names = {"--from"},
            description = "Дата начала (ISO8601 format)")
    private String from;

    @CommandLine.Option(
            names = {"--to"},
            description = "Дата конца (ISO8601 format)")
    private String to;

    @Override
    public Integer call() {
        try {
            logger.info("Запуск анализа логов");
            logger.info("Пути, поданные на вход: {}", String.join(", ", paths));
            logger.info("Выходной формате: {}", format);
            logger.info("Выходной файл: {}", output);

            validateFormat();
            OutputFileValidator.validate(output, format);

            LocalDate fromDate = parseDate(from);
            LocalDate toDate = parseDate(to);
            validateDateRange(fromDate, toDate);

            logger.info("Сбор источников логов");
            var logSources = LogSourceProvider.resolveSources(paths);

            if (logSources.isEmpty()) {
                logger.error("Файлов с логами не найдено");
                return 2;
            }

            logger.info("Найдено {} источников логов", logSources.size());
            for (var source : logSources) {
                logger.info("Источник логов: {}", source.getDescription());
            }

            logger.info("Начало сбора статистики");
            var collector = new LogStatisticsCollector(fromDate, toDate);
            for (var source : logSources) {
                try (var lineStream = source.getLineStream()) {
                    NginxLogParser.parseStream(lineStream, collector);
                }
            }

            LogAnalysisResult result = collector.buildResult(logSources);

            ReportFormatter formatter = FormatterFactory.createFormatter(format);
            String report = formatter.format(result);

            ReportWriter.write(output, report);

            return 0;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации: {}", e.getMessage());
            return 2;
        } catch (Exception e) {
            logger.error("Неизвестная ошибка", e);
            return 1;
        }
    }

    private void validateFormat() {
        if (!format.matches("json|markdown|adoc")) {
            throw new IllegalArgumentException("Неподдерживаемый формат: " + format + ". Поддерживаемые: json, markdown, adoc");
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Недопустимый формат даты: " + dateStr + ". Ожидается формат ISO8601 (yyyy-MM-dd)");
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && !fromDate.isBefore(toDate)) {
            throw new IllegalArgumentException("Дата с (from) должна быть до даты до (to)");
        }
    }
}
