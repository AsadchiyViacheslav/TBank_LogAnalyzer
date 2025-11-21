package academy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NginxLogEntry {
    private final String remoteAddr;
    private final LocalDateTime timestamp;
    private final String method;
    private final String resource;
    private final String protocol;
    private final int statusCode;
    private final long bodyBytes;
}
