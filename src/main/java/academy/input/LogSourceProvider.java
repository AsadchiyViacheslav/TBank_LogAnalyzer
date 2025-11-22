package academy.input;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogSourceProvider {
    private static final Logger logger = LogManager.getLogger(LogSourceProvider.class);

    private static final String LOG_EXTENSION = ".log";
    private static final String TXT_EXTENSION = ".txt";
    private static final String GLOB_PREFIX = "glob:";
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String CURRENT_DIR = ".";
    private static final String WILDCARD = "*";

    public static List<LogSource> resolveSources(String[] paths) throws IOException {
        List<LogSource> sources = new ArrayList<>();

        for (String pathStr : paths) {
            if (isUrl(pathStr)) {
                logger.info("Найден URL источник: {}", pathStr);
                sources.add(new RemoteLogSource(pathStr));
            } else {
                logger.info("Найден Local источник: {}", pathStr);
                sources.addAll(resolveLocalPath(pathStr));
            }
        }

        return sources;
    }

    private static List<LogSource> resolveLocalPath(String pathStr) throws IOException {
        List<LogSource> sources = new ArrayList<>();

        if (pathStr.contains(WILDCARD)) {
            Path basePath = getBasePathFromGlob(pathStr);
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(GLOB_PREFIX + pathStr);

            try (var stream = Files.walk(basePath)) {
                stream.filter(Files::isRegularFile).filter(matcher::matches).forEach(p -> {
                    validateFileExtension(p.toString());
                    sources.add(new LocalLogSource(p));
                });
            }
        } else {
            validateFileExtension(pathStr);
            Path path = Path.of(pathStr);
            if (!Files.exists(path)) {
                throw new IOException("Файл не найден: " + pathStr);
            }
            sources.add(new LocalLogSource(path));
        }

        return sources;
    }

    private static Path getBasePathFromGlob(String pathStr) {
        int starIndex = pathStr.indexOf(WILDCARD);
        if (starIndex == -1) {
            return Path.of(pathStr);
        }

        String basePart = pathStr.substring(0, starIndex);
        int lastSeparator = Math.max(basePart.lastIndexOf('/'), basePart.lastIndexOf('\\'));

        if (lastSeparator == -1) {
            return Path.of(CURRENT_DIR);
        }

        String basePath = pathStr.substring(0, lastSeparator);
        return Path.of(basePath);
    }

    private static void validateFileExtension(String path) {
        if (!path.endsWith(LOG_EXTENSION) && !path.endsWith(TXT_EXTENSION)) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла: " + path);
        }
    }

    private static boolean isUrl(String path) {
        return path.startsWith(HTTP_PREFIX) || path.startsWith(HTTPS_PREFIX);
    }
}
