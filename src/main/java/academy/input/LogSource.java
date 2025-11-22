package academy.input;

import java.io.IOException;
import java.util.stream.Stream;

public interface LogSource {
    Stream<String> getLineStream() throws IOException;

    String getDescription();
}
