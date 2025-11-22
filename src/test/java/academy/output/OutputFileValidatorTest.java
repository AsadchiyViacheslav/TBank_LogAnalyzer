package academy.output;

import static org.assertj.core.api.Assertions.*;

import academy.enums.ReportFileType;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OutputFileValidatorTest {

    private Path createTempDir() throws IOException {
        return Files.createTempDirectory("test-");
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }

    private void validate(String path, ReportFileType type) {
        assertThatCode(() -> OutputFileValidator.validate(path, type))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Валидный выходной файл проходит валидацию")
    void testValidOutputFile() throws IOException {
        Path dir = createTempDir();
        Path file = dir.resolve("report.json");

        validate(file.toString(), ReportFileType.JSON);

        deleteQuietly(file);
        deleteQuietly(dir);
    }

    @Test
    @DisplayName("Несовпадающее расширение выбрасывает исключение")
    void testMismatchedExtension() throws IOException {
        Path dir = createTempDir();
        Path file = dir.resolve("report.txt");

        assertThatThrownBy(() -> OutputFileValidator.validate(file.toString(), ReportFileType.JSON))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Расширение выходного файла должно быть");

        deleteQuietly(dir);
    }

    @Test
    @DisplayName("Существующий файл выбрасывает исключение")
    void testExistingFileThrows() throws IOException {
        Path existing = Files.createTempFile("test-", ".json");

        try {
            assertThatThrownBy(() -> OutputFileValidator.validate(existing.toString(), ReportFileType.JSON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Выходной файл уже существует");
        } finally {
            deleteQuietly(existing);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"json", "md", "adoc"})
    @DisplayName("Проверка расширения для всех форматов")
    void testAllFormats(String ext) throws IOException {
        Path dir = createTempDir();
        ReportFileType type = switch (ext) {
            case "json" -> ReportFileType.JSON;
            case "md"   -> ReportFileType.MARKDOWN;
            case "adoc" -> ReportFileType.ADOC;
            default -> throw new IllegalStateException();
        };

        Path file = dir.resolve("report." + ext);

        validate(file.toString(), type);

        deleteQuietly(dir);
    }
}
