package academy.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LocalLogSource implements LogSource {
    private final Path path;

    public LocalLogSource(Path path) {
        this.path = path;
    }

    @Override
    public Stream<String> getLineStream() throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Файл не найден: " + path);
        }
        return Files.lines(path);
    }

    @Override
    public String getDescription() {
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : path.toString();
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
