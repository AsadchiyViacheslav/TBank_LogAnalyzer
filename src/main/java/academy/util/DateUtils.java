package academy.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class DateUtils {

    private DateUtils() {}

    public static LocalDate parse(String dateStr) {
        if (dateStr == null) return null;

        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Недопустимый формат даты: " + dateStr);
        }
    }

    public static void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("Дата начала должна быть до даты конца");
        }
    }

    public static boolean isInRange(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null) return false;

        if (from != null && date.isBefore(from)) return false;

        return to == null || !date.isAfter(to);
    }
}
