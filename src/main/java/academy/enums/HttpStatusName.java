package academy.enums;

import lombok.Getter;

@Getter
public enum HttpStatusName {
    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),

    UNKNOWN(-1, "Unknown");

    private final int code;
    private final String name;

    HttpStatusName(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static HttpStatusName fromCode(int code) {
        for (HttpStatusName s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return UNKNOWN;
    }
}
