package academy.util;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DateUtilsTest {

    private LocalDate from;
    private LocalDate to;
    private LocalDate mid;

    @BeforeEach
    void setUp() {
        from = LocalDate.of(2025, 1, 1);
        to   = LocalDate.of(2025, 1, 31);
        mid  = LocalDate.of(2025, 1, 15);
    }

    @Test
    @DisplayName("Валидная дата в формате ISO8601")
    void testParseValidDate() {
        assertThat(DateUtils.parse("2025-01-15"))
            .isEqualTo(LocalDate.of(2025, 1, 15));
    }

    @Test
    @DisplayName("Null дата возвращает null")
    void testParseNullDate() {
        assertThat(DateUtils.parse(null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025/01/01", "01-01-2025", "today", "2025.01.01"})
    @DisplayName("Невалидный формат даты выбрасывает исключение")
    void testParseInvalidFormat(String invalidDate) {
        assertThatThrownBy(() -> DateUtils.parse(invalidDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Недопустимый формат даты");
    }

    @Test
    @DisplayName("Диапазон дат - корректный диапазон")
    void testValidateRangeValid() {
        assertThatCode(() -> DateUtils.validateRange(from, to)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Диапазон дат - from равен to")
    void testValidateRangeEqual() {
        assertThatThrownBy(() -> DateUtils.validateRange(from, from))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Дата начала должна быть до даты конца");
    }

    @Test
    @DisplayName("Диапазон дат - from > to")
    void testValidateRangeInvalid() {
        assertThatThrownBy(() -> DateUtils.validateRange(to, from))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Дата в диапазоне - внутри диапазона")
    void testIsInRangeInside() {
        assertThat(DateUtils.isInRange(mid, from, to)).isTrue();
    }

    @Test
    @DisplayName("Дата в диапазоне - до начала")
    void testIsInRangeBefore() {
        assertThat(DateUtils.isInRange(from.minusDays(1), from, to)).isFalse();
    }

    @Test
    @DisplayName("Дата в диапазоне - после конца")
    void testIsInRangeAfter() {
        assertThat(DateUtils.isInRange(to.plusDays(1), from, to)).isFalse();
    }

    @Test
    @DisplayName("Дата в диапазоне - без from")
    void testIsInRangeWithoutFrom() {
        assertThat(DateUtils.isInRange(mid, null, to)).isTrue();
    }
}
