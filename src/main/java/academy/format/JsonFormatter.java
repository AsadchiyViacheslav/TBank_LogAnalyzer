package academy.format;

import academy.model.LogAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonFormatter implements ReportFormatter {
    private final ObjectWriter writer;

    public JsonFormatter() {
        ObjectMapper mapper = new ObjectMapper();
        this.writer = mapper.writer(new CompactPrettyPrinter());
    }

    @Override
    public String format(LogAnalysisResult result) {
        try {
            return writer.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации в JSON", e);
        }
    }
}
