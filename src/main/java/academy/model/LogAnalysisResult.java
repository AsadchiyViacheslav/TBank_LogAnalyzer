package academy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LogAnalysisResult {
    private final List<String> files;
    private final long totalRequestsCount;
    private final ResponseSizeStats responseSizeInBytes;
    private final List<ResourceStat> resources;
    private final List<ResponseCodeStat> responseCodes;
    private final List<DailyRequestStat> requestsPerDate;
    private final List<String> uniqueProtocols;

    @Getter
    @AllArgsConstructor
    public static class ResponseSizeStats {
        private final double average;
        private final double max;
        private final double p95;
    }

    @Getter
    @AllArgsConstructor
    public static class ResourceStat {
        private final String resource;
        private final long totalRequestsCount;
    }

    @Getter
    @AllArgsConstructor
    public static class ResponseCodeStat {
        private final int code;
        private final long totalResponsesCount;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyRequestStat {
        private final String date;
        private final String weekday;
        private final long totalRequestsCount;
        private final double totalRequestsPercentage;
    }
}
