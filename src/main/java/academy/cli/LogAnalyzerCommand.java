package academy.cli;

import academy.cli.converter.ReportFileTypeConverter;
import academy.enums.ReportFileType;
import academy.format.ReportFormatter;
import academy.format.factory.FormatterFactory;
import academy.input.LogSourceProvider;
import academy.model.LogAnalysisResult;
import academy.output.OutputFileValidator;
import academy.output.ReportWriter;
import academy.parser.NginxLogParser;
import academy.stats.LogStatisticsCollector;
import academy.util.DateUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "log-analyzer",
        version = "1.0.0",
        description = "Утилита для анализа логов NGINX",
        mixinStandardHelpOptions = true)
public class LogAnalyzerCommand implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);

    @Option(
            names = {"-p", "--path"},
            required = true,
            arity = "1..*",
            description = "Пути к файлам логов NGINX")
    private String[] paths;

    @Option(
        names = {"-f", "--format"},
        required = true,
        converter = ReportFileTypeConverter.class,
        description = "Выходной формат: json, markdown, adoc")
    private ReportFileType format;

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "Путь к выходному файлу")
    private String output;

    @Option(
            names = {"--from"},
            description = "Дата начала (ISO8601 format)")
    private String from;

    @Option(
            names = {"--to"},
            description = "Дата конца (ISO8601 format)")
    private String to;

    @Override
    public Integer call() {
        try {
            logger.info("Запуск анализа логов");
            logger.info("Пути: {}", String.join(", ", paths));
            logger.info("Выходной формат: {}", format);
            logger.info("Выходной файл: {}", output);

            OutputFileValidator.validate(output, format);

            LocalDate fromDate = DateUtils.parse(from);
            LocalDate toDate = DateUtils.parse(to);
            DateUtils.validateRange(fromDate, toDate);

            var logSources = LogSourceProvider.resolveSources(paths);
            if (logSources.isEmpty()) {
                throw new IOException("Файлов с логами не найдено");
            }

            var collector = new LogStatisticsCollector(fromDate, toDate);
            for (var source : logSources) {
                try (var lineStream = source.getLineStream()) {
                    NginxLogParser.parseStream(lineStream, collector);
                }
            }

            LogAnalysisResult result = collector.buildResult(logSources);
            ReportFormatter formatterObj = FormatterFactory.createFormatter(format);
            String report = formatterObj.format(result);

            ReportWriter.write(output, report);

            return 0;
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации: {}", e.getMessage());
            return 2;
        } catch (IOException e) {
            logger.error("Ошибка ввода: {}", e.getMessage());
            return 2;
        } catch (Exception e) {
            logger.error("Неизвестная ошибка", e);
            return 1;
        }
    }
}
