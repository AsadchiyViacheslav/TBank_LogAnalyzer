package academy.input;

import academy.exception.UserInputException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RemoteLogSource implements LogSource {
    private static final Logger logger = LogManager.getLogger(RemoteLogSource.class);
    private final String url;

    public RemoteLogSource(String url) {
        this.url = url;
    }

    /*
     * Анализатор SpotBugs (правило OS_OPEN_STREAM) помечает этот метод как потенциальную утечку ресурсов,
     * так как BufferedReader не обернут в конструкцию try-with-resources.
     *
     * Однако, для реализации ленивого (потокового) построчного чтения (reader.lines())
     * BufferedReader должен оставаться открытым до тех пор, пока внешний Stream не будет закрыт.
     *
     * Закрытие всех ресурсов (BufferedReader и HttpURLConnection) явно гарантировано
     * вызовом .onClose() на возвращаемом Stream, поэтому добавил подавление предупреждения.
     */
    @Override
    @SuppressFBWarnings("OS_OPEN_STREAM")
    public Stream<String> getLineStream() throws IOException, UserInputException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new UserInputException("Невалидный URL: " + url, e);
        }

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode == 404) {
            conn.disconnect();
            throw new UserInputException("Удалённый файл не найден (404): " + url);
        }
        if (responseCode < 200 || responseCode >= 300) {
            conn.disconnect();
            throw new UserInputException("Не удалось получить удалённый файл. Код: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        return reader.lines().onClose(() -> {
            try {
                reader.close();
            } catch (IOException e) {
                logger.warn("Ошибка при закрытии reader для URL: {}", url, e);
            } finally {
                conn.disconnect();
            }
        });
    }

    @Override
    public String getDescription() {
        return url;
    }

    @Override
    public String toString() {
        return url;
    }
}
