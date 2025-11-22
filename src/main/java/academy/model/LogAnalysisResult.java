package academy.model;

import java.util.List;

public record LogAnalysisResult(
    List<String> files,
    long totalRequestsCount,
    ResponseSizeStats responseSizeInBytes,
    List<ResourceStat> resources,
    List<ResponseCodeStat> responseCodes,
    List<DailyRequestStat> requestsPerDate,
    List<String> uniqueProtocols) {
}
