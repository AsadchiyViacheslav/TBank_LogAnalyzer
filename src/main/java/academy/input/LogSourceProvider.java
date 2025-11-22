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

    public static List<LogSource> resolveSources(String[] paths) throws IOException {
        List<LogSource> sources = new ArrayList<>();

        for (String pathStr : paths) {
            if (isUrl(pathStr)) {
                logger.info("Найден Url источник: {}", pathStr);
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

        if (pathStr.contains("*")) {
            Path basePath = getBasePathFromGlob(pathStr);

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pathStr);

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

        int starIndex = pathStr.indexOf('*');
        if (starIndex == -1) {
            return Path.of(pathStr);
        }

        String basePart = pathStr.substring(0, starIndex);
        int lastSeparator = Math.max(basePart.lastIndexOf('/'), basePart.lastIndexOf('\\'));

        if (lastSeparator == -1) {
            return Path.of(".");
        }

        String basePath = pathStr.substring(0, lastSeparator);
        return Path.of(basePath);
    }

    private static void validateFileExtension(String path) {
        if (!path.endsWith(".log") && !path.endsWith(".txt")) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла: " + path);
        }
    }

    private static boolean isUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }
}
