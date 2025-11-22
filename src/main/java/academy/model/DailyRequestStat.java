package academy.model;

public record DailyRequestStat(String date, String weekday, long totalRequestsCount, double totalRequestsPercentage) {}
