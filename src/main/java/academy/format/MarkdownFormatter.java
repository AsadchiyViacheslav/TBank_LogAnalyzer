package academy.format;

import academy.model.LogAnalysisResult;

public class MarkdownFormatter extends BaseReportFormatter {

    private static final String HEADER_MAIN = "# Анализ логов NGINX\n\n";
    private static final String HEADER_GENERAL = "### Общая информация\n\n";
    private static final String HEADER_RESOURCES = "### Запрашиваемые ресурсы\n\n";
    private static final String HEADER_RESPONSE_CODES = "### Коды ответа\n\n";
    private static final String HEADER_DAILY = "### Распределение запросов по датам\n\n";
    private static final String HEADER_PROTOCOLS = "### Используемые протоколы\n\n";

    private static final String TABLE_SEPARATOR = "|";
    private static final String TABLE_HEADER_GENERAL = "|:-------:|---------:|";
    private static final String TABLE_HEADER_RESOURCES = "|--------|------------|";
    private static final String TABLE_HEADER_RESPONSE_CODES = "|-----|----------|------------|";
    private static final String TABLE_HEADER_DAILY = "|------|-------------|------------|---------|";

    private static final String CODE_WRAP = "`";
    private static final String BULLET = "- ";

    @Override
    public String format(LogAnalysisResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append(HEADER_MAIN);

        appendGeneralInfo(sb, result);
        appendResources(sb, result);
        appendResponseCodes(sb, result);
        appendDailyDistribution(sb, result);
        appendProtocols(sb, result);

        return sb.toString();
    }

    private void appendGeneralInfo(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_GENERAL);
        sb.append(TABLE_SEPARATOR)
                .append(" Метрика ")
                .append(TABLE_SEPARATOR)
                .append(" Значение ")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_HEADER_GENERAL).append("\n");

        String files = String.join(", ", result.files());
        sb.append(TABLE_SEPARATOR)
                .append(" Файл(-ы) ")
                .append(TABLE_SEPARATOR)
                .append(CODE_WRAP)
                .append(files)
                .append(CODE_WRAP)
                .append(TABLE_SEPARATOR)
                .append("\n");

        var min = getMinDate(result);
        var max = getMaxDate(result);
        sb.append(TABLE_SEPARATOR)
                .append(" Начальная дата ")
                .append(TABLE_SEPARATOR)
                .append(min != null ? min : "-")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_SEPARATOR)
                .append(" Конечная дата ")
                .append(TABLE_SEPARATOR)
                .append(max != null ? max : "-")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_SEPARATOR)
                .append(" Количество запросов ")
                .append(TABLE_SEPARATOR)
                .append(formatNumber(result.totalRequestsCount()))
                .append(TABLE_SEPARATOR)
                .append("\n");

        var stats = result.responseSizeInBytes();
        sb.append(TABLE_SEPARATOR)
                .append(" Средний размер ответа ")
                .append(TABLE_SEPARATOR)
                .append(stats.average())
                .append("b")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_SEPARATOR)
                .append(" Максимальный размер ответа ")
                .append(TABLE_SEPARATOR)
                .append(formatNumber((long) stats.max()))
                .append("b")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_SEPARATOR)
                .append(" 95p размера ответа ")
                .append(TABLE_SEPARATOR)
                .append(stats.p95())
                .append("b")
                .append(TABLE_SEPARATOR)
                .append("\n\n");
    }

    private void appendResources(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_RESOURCES);
        sb.append(TABLE_SEPARATOR)
                .append(" Ресурс ")
                .append(TABLE_SEPARATOR)
                .append(" Количество ")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_HEADER_RESOURCES).append("\n");

        for (var r : result.resources()) {
            sb.append(TABLE_SEPARATOR)
                    .append(CODE_WRAP)
                    .append(r.resource())
                    .append(CODE_WRAP)
                    .append(TABLE_SEPARATOR)
                    .append(formatNumber(r.totalRequestsCount()))
                    .append(TABLE_SEPARATOR)
                    .append("\n");
        }
        sb.append("\n");
    }

    private void appendResponseCodes(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_RESPONSE_CODES);
        sb.append(TABLE_SEPARATOR)
                .append(" Код ")
                .append(TABLE_SEPARATOR)
                .append(" Название ")
                .append(TABLE_SEPARATOR)
                .append(" Количество ")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_HEADER_RESPONSE_CODES).append("\n");

        for (var rc : result.responseCodes()) {
            sb.append(TABLE_SEPARATOR)
                    .append(rc.code())
                    .append(TABLE_SEPARATOR)
                    .append(getHttpStatusName(rc.code()))
                    .append(TABLE_SEPARATOR)
                    .append(formatNumber(rc.totalResponsesCount()))
                    .append(TABLE_SEPARATOR)
                    .append("\n");
        }
        sb.append("\n");
    }

    private void appendDailyDistribution(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_DAILY);
        sb.append(TABLE_SEPARATOR)
                .append(" Дата ")
                .append(TABLE_SEPARATOR)
                .append(" День недели ")
                .append(TABLE_SEPARATOR)
                .append(" Количество ")
                .append(TABLE_SEPARATOR)
                .append(" Процент ")
                .append(TABLE_SEPARATOR)
                .append("\n");
        sb.append(TABLE_HEADER_DAILY).append("\n");

        for (var d : result.requestsPerDate()) {
            sb.append(TABLE_SEPARATOR)
                    .append(d.date())
                    .append(TABLE_SEPARATOR)
                    .append(d.weekday())
                    .append(TABLE_SEPARATOR)
                    .append(formatNumber(d.totalRequestsCount()))
                    .append(TABLE_SEPARATOR)
                    .append(String.format("%.2f", d.totalRequestsPercentage()))
                    .append("%")
                    .append(TABLE_SEPARATOR)
                    .append("\n");
        }
        sb.append("\n");
    }

    private void appendProtocols(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_PROTOCOLS);
        for (var protocol : result.uniqueProtocols()) {
            sb.append(BULLET).append(protocol).append("\n");
        }
    }
}
