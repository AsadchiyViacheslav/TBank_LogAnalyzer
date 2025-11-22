package academy.format;

import academy.model.LogAnalysisResult;

public class AsciiDocFormatter extends BaseReportFormatter {

    private static final String HEADER_MAIN = "= Анализ логов NGINX\n\n";
    private static final String HEADER_GENERAL = "=== Общая информация\n\n";
    private static final String HEADER_RESOURCES = "=== Статистика по ресурсам\n\n";
    private static final String HEADER_RESPONSE_CODES = "=== Коды ответа\n\n";
    private static final String HEADER_DAILY = "=== Распределение запросов по датам\n\n";
    private static final String HEADER_PROTOCOLS = "=== Используемые протоколы\n\n";

    private static final String TABLE_START = "|====";
    private static final String TABLE_END = "|====";
    private static final String HEADER_OPTION_2COLS = "[cols=\"1,1\", options=\"header\"]";
    private static final String HEADER_OPTION_3COLS = "[cols=\"1,1,1\", options=\"header\"]";
    private static final String HEADER_OPTION_4COLS = "[cols=\"1,1,1,1\", options=\"header\"]";

    private static final String CODE_WRAP = "`";
    private static final String BULLET = "* ";

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
        sb.append(HEADER_OPTION_2COLS).append("\n").append(TABLE_START).append("\n");
        sb.append("| Метрика | Значение\n");

        String files = String.join(
                ", ",
                result.files().stream().map(f -> CODE_WRAP + f + CODE_WRAP).toList());
        sb.append("| Файл(-ы) | ").append(files).append("\n");

        var min = getMinDate(result);
        var max = getMaxDate(result);
        sb.append("| Начальная дата | ").append(min != null ? min : "-").append("\n");
        sb.append("| Конечная дата | ").append(max != null ? max : "-").append("\n");
        sb.append("| Количество запросов | ")
                .append(formatNumber(result.totalRequestsCount()))
                .append("\n");

        var s = result.responseSizeInBytes();
        sb.append("| Средний размер ответа | ").append(s.average()).append("b\n");
        sb.append("| Максимальный размер ответа | ")
                .append(formatNumber((long) s.max()))
                .append("b\n");
        sb.append("| 95-й перцентиль | ").append(s.p95()).append("b\n");

        sb.append(TABLE_END).append("\n\n");
    }

    private void appendResources(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_RESOURCES);
        sb.append(HEADER_OPTION_2COLS).append("\n").append(TABLE_START).append("\n");
        sb.append("| Ресурс | Количество\n");

        for (var r : result.resources()) {
            sb.append("| ")
                    .append(CODE_WRAP)
                    .append(r.resource())
                    .append(CODE_WRAP)
                    .append(" | ")
                    .append(formatNumber(r.totalRequestsCount()))
                    .append("\n");
        }

        sb.append(TABLE_END).append("\n\n");
    }

    private void appendResponseCodes(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_RESPONSE_CODES);
        sb.append(HEADER_OPTION_3COLS).append("\n").append(TABLE_START).append("\n");
        sb.append("| Код | Название | Количество\n");

        for (var rc : result.responseCodes()) {
            sb.append("| ")
                    .append(rc.code())
                    .append(" | ")
                    .append(getHttpStatusName(rc.code()))
                    .append(" | ")
                    .append(formatNumber(rc.totalResponsesCount()))
                    .append("\n");
        }

        sb.append(TABLE_END).append("\n\n");
    }

    private void appendDailyDistribution(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_DAILY);
        sb.append(HEADER_OPTION_4COLS).append("\n").append(TABLE_START).append("\n");
        sb.append("| Дата | День недели | Количество | Процент\n");

        for (var d : result.requestsPerDate()) {
            sb.append("| ")
                    .append(d.date())
                    .append(" | ")
                    .append(d.weekday())
                    .append(" | ")
                    .append(formatNumber(d.totalRequestsCount()))
                    .append(" | ")
                    .append(String.format("%.2f", d.totalRequestsPercentage()))
                    .append("%\n");
        }

        sb.append(TABLE_END).append("\n\n");
    }

    private void appendProtocols(StringBuilder sb, LogAnalysisResult result) {
        sb.append(HEADER_PROTOCOLS);
        for (var p : result.uniqueProtocols()) {
            sb.append(BULLET).append(p).append("\n");
        }
    }
}
