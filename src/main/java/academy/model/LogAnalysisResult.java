package academy.model;

import java.util.List;

public record LogAnalysisResult(
        List<String> files,
        long totalRequestsCount,
        academy.model.LogAnalysisResult.ResponseSizeStats responseSizeInBytes,
        List<ResourceStat> resources,
        List<ResponseCodeStat> responseCodes,
        List<DailyRequestStat> requestsPerDate,
        List<String> uniqueProtocols) {

    public record ResponseSizeStats(double average, double max, double p95) {}

    public record ResourceStat(String resource, long totalRequestsCount) {}

    public record ResponseCodeStat(int code, long totalResponsesCount) {}

    public record DailyRequestStat(
            String date, String weekday, long totalRequestsCount, double totalRequestsPercentage) {}
}
