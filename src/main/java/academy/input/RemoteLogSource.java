package academy.input;

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

    @Override
    public Stream<String> getLineStream() throws IOException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IOException("Невалидный URL: " + url, e);
        }

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode == 404) {
            conn.disconnect();
            throw new IOException("Удалённый файл не найден (404): " + url);
        }
        if (responseCode != 200) {
            conn.disconnect();
            throw new IOException("Не удалось получить удалённый файл. Код: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

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
