package academy.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

        java.net.URI uri;
        try {
            uri = new java.net.URI(url);
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Невалидный URL: " + url, e);
        }

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode == 404) {
            throw new IOException("Удаленный файл не найден (404): " + url);
        }
        if (responseCode != 200) {
            throw new IOException("Не удалось получить удаленный файл. Код состояния: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        return reader.lines().onClose(() -> {
            try {
                reader.close();
                conn.disconnect();
            } catch (IOException e) {
                logger.warn("Ошибка при закрытии соединения", e);
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
