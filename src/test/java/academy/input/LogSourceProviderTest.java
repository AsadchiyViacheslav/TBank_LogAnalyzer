package academy.input;

import static org.assertj.core.api.Assertions.*;

import academy.util.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogSourceProviderTest {
    @Test
    @DisplayName("Разрешение локального файла .log")
    void testResolveLocalLogFile() throws IOException {
        File logFile = TestUtils.createTempLogFile();

        try {
            List<LogSource> sources = resolve(logFile.getAbsolutePath());

            assertThat(sources).hasSize(1);
            assertThat(sources.getFirst()).isInstanceOf(LocalLogSource.class);
        } finally {
            deleteQuietly(logFile);
        }
    }

    @Test
    @DisplayName("Несуществующий файл выбрасывает исключение")
    void testNonExistentFileThrows() {
        assertThatThrownBy(() -> resolve("/nonexistent/file.log")).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Неподдерживаемый формат файла выбрасывает исключение")
    void testUnsupportedFileFormatThrows() throws IOException {
        File badFile = File.createTempFile("test-", ".docx");

        try {
            assertThatThrownBy(() -> resolve(badFile.getAbsolutePath()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Неподдерживаемый формат файла");
        } finally {
            deleteQuietly(badFile);
        }
    }

    private void deleteQuietly(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException ignored) {
        }
    }

    private List<LogSource> resolve(String... paths) throws IOException {
        return LogSourceProvider.resolveSources(paths);
    }
}
