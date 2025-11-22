package academy.io;

import java.io.IOException;
import java.util.stream.Stream;

public interface LogSource {
    Stream<String> getLineStream() throws IOException;

    String getDescription();
}
