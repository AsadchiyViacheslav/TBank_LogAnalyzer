package academy.format;

import academy.model.LogAnalysisResult;

public class MarkdownFormatter extends BaseReportFormatter {

    @Override
    public String format(LogAnalysisResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Анализ логов NGINX\n\n");

        appendGeneralInfo(sb, result);
        appendResources(sb, result);
        appendResponseCodes(sb, result);
        appendDailyDistribution(sb, result);
        appendProtocols(sb, result);

        return sb.toString();
    }

    private void appendGeneralInfo(StringBuilder sb, LogAnalysisResult result) {
        sb.append("### Общая информация\n\n");
        sb.append("| Метрика | Значение |\n");
        sb.append("|:-------:|---------:|\n");

        String files = String.join(", ", result.files());
        sb.append("| Файл(-ы) | `").append(files).append("` |\n");

        var min = getMinDate(result);
        var max = getMaxDate(result);

        sb.append("| Начальная дата | ").append(min != null ? min : "-").append(" |\n");
        sb.append("| Конечная дата | ").append(max != null ? max : "-").append(" |\n");
        sb.append("| Количество запросов | ")
                .append(formatNumber(result.totalRequestsCount()))
                .append(" |\n");

        var stats = result.responseSizeInBytes();
        sb.append("| Средний размер ответа | ").append(stats.average()).append("b |\n");
        sb.append("| Максимальный размер ответа | ")
                .append(formatNumber((long) stats.max()))
                .append("b |\n");
        sb.append("| 95p размера ответа | ").append(stats.p95()).append("b |\n\n");
    }

    private void appendResources(StringBuilder sb, LogAnalysisResult result) {
        sb.append("### Запрашиваемые ресурсы\n\n");
        sb.append("| Ресурс | Количество |\n");
        sb.append("|--------|------------|\n");

        for (var r : result.resources()) {
            sb.append("| `")
                    .append(r.resource())
                    .append("` | ")
                    .append(formatNumber(r.totalRequestsCount()))
                    .append(" |\n");
        }
        sb.append("\n");
    }

    private void appendResponseCodes(StringBuilder sb, LogAnalysisResult result) {
        sb.append("### Коды ответа\n\n");
        sb.append("| Код | Название | Количество |\n");
        sb.append("|-----|----------|------------|\n");

        for (var rc : result.responseCodes()) {
            sb.append("| ")
                    .append(rc.code())
                    .append(" | ")
                    .append(getHttpStatusName(rc.code()))
                    .append(" | ")
                    .append(formatNumber(rc.totalResponsesCount()))
                    .append(" |\n");
        }
        sb.append("\n");
    }

    private void appendDailyDistribution(StringBuilder sb, LogAnalysisResult result) {
        sb.append("### Распределение запросов по датам\n\n");
        sb.append("| Дата | День недели | Количество | Процент |\n");
        sb.append("|------|-------------|------------|---------|\n");

        for (var d : result.requestsPerDate()) {
            sb.append("| ")
                    .append(d.date())
                    .append(" | ")
                    .append(d.weekday())
                    .append(" | ")
                    .append(formatNumber(d.totalRequestsCount()))
                    .append(" | ")
                    .append(String.format("%.2f", d.totalRequestsPercentage()))
                    .append("% |\n");
        }
        sb.append("\n");
    }

    private void appendProtocols(StringBuilder sb, LogAnalysisResult result) {
        sb.append("### Используемые протоколы\n\n");
        for (var protocol : result.uniqueProtocols()) {
            sb.append("- ").append(protocol).append("\n");
        }
    }
}
