package academy.format;

import academy.enums.HttpStatusName;
import academy.model.LogAnalysisResult;
import java.time.LocalDate;

public abstract class BaseReportFormatter implements ReportFormatter {

    protected String formatNumber(long num) {
        return String.format("%,d", num).replace(",", "_");
    }

    protected String getHttpStatusName(int code) {
        return HttpStatusName.fromCode(code).getName();
    }

    protected LocalDate getMinDate(LogAnalysisResult result) {
        if (result.requestsPerDate().isEmpty()) return null;
        return LocalDate.parse(result.requestsPerDate().getFirst().date());
    }

    protected LocalDate getMaxDate(LogAnalysisResult result) {
        if (result.requestsPerDate().isEmpty()) return null;
        var list = result.requestsPerDate();
        return LocalDate.parse(list.getLast().date());
    }
}
