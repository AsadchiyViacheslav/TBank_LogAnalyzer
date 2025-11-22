package academy.format;

import academy.model.LogAnalysisResult;
import academy.util.HttpStatusNames;
import java.time.LocalDate;

public abstract class BaseReportFormatter implements ReportFormatter {

    protected String formatNumber(long num) {
        return String.format("%,d", num).replace(",", "_");
    }

    protected String getHttpStatusName(int code) {
        return HttpStatusNames.getName(code);
    }

    protected LocalDate getMinDate(LogAnalysisResult result) {
        if (result.requestsPerDate().isEmpty()) return null;
        return LocalDate.parse(result.requestsPerDate().get(0).date());
    }

    protected LocalDate getMaxDate(LogAnalysisResult result) {
        if (result.requestsPerDate().isEmpty()) return null;
        var list = result.requestsPerDate();
        return LocalDate.parse(list.get(list.size() - 1).date());
    }
}
