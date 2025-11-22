package academy.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class CompactPrettyPrinter extends DefaultPrettyPrinter {

    public CompactPrettyPrinter() {
        super();

        // элементы массива с новой строки
        this.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return new CompactPrettyPrinter();
    }

    // убирает пробел после значения поля перед ":"
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) {
        try {
            g.writeRaw(": ");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
