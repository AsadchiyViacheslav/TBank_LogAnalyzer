package academy.format;

import academy.model.LogAnalysisResult;

public interface ReportFormatter {
    String format(LogAnalysisResult result);
}
