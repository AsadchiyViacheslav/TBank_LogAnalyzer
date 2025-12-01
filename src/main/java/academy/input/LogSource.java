package academy.input;

import academy.exception.UserInputException;
import java.io.IOException;
import java.util.stream.Stream;

public interface LogSource {
    Stream<String> getLineStream() throws IOException, UserInputException;

    String getDescription();
}
