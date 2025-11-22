package academy.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

public class RemoteLogSource implements LogSource {
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

        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                throw new IOException("Удалённый файл не найден (404): " + url);
            }
            if (responseCode != 200) {
                throw new IOException("Не удалось получить удалённый файл. Код: " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                List<String> lines = reader.lines().toList();
                return lines.stream();
            }

        } finally {
            conn.disconnect();
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
