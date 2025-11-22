package academy.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {
    public static final String[] TEST_LOG_LINES = {
        "173.11.48.149 - - [23/May/2015:04:05:19 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 331 \"-\" \"Debian APT-HTTP/1.3 (1.0.1ubuntu2)\"",
        "185.13.90.131 - - [23/May/2015:06:05:49 +0000] \"GET /downloads/product_2 HTTP/1.1\" 404 336 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
        "192.168.241.116 - - [25/May/2015:22:05:57 +0000] \"GET /downloads/product_2 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
        "80.70.214.71 - - [17/May/2015:10:05:23 +0000] \"HEAD /downloads/product_2 HTTP/1.1\" 200 0 \"-\" \"Wget/1.13.4 (linux-gnu)\""
    };

    public static final long TOTAL_REQUESTS = 4;
    public static final double AVERAGE_RESPONSE_SIZE = 166.75;
    public static final double MAX_RESPONSE_SIZE = 336.0;
    public static final double P95_RESPONSE_SIZE = 335.25;
    public static final int UNIQUE_RESOURCES = 2;
    public static final int UNIQUE_STATUS_CODES = 3;

    public static File createTempLogFile(String... lines) throws IOException {
        File file = File.createTempFile("test-", ".log");
        file.deleteOnExit();
        Files.write(file.toPath(), String.join("\n", lines).getBytes());
        return file;
    }

    public static File createTempLogFile() throws IOException {
        return createTempLogFile(TEST_LOG_LINES);
    }

    public static String createInvalidLogLine() {
        return "this is not a valid nginx log line";
    }

    public static String createOutputPath(String extension) throws IOException {
        Path tempDir = Files.createTempDirectory("output-");
        return tempDir.resolve("report." + extension).toString();
    }
}
