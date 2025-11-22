package academy.model;

import java.time.LocalDateTime;

public record NginxLogEntry(
        String remoteAddr,
        LocalDateTime timestamp,
        String method,
        String resource,
        String protocol,
        int statusCode,
        long bodyBytes) {}
