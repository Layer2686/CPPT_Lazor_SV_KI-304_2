package ua.lpnu.kzp.lab05;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Контракт плоского метеозапису та анотацій його експортованих полів. */
class WeatherReadingRecordTest
{
    @Test
    void annotationHasRuntimeFieldMetadataAndExpectedColumnNames()
    {
        assertEquals(RetentionPolicy.RUNTIME, CsvColumn.class.getAnnotation(Retention.class).value());
        assertEquals(List.of(ElementType.FIELD), Arrays.asList(CsvColumn.class.getAnnotation(Target.class).value()));
        List<String> columns = Arrays.stream(WeatherReadingRecord.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                .sorted(Comparator.comparing(Field::getName))
                .map(field -> field.getAnnotation(CsvColumn.class).value())
                .toList();

        assertEquals(List.of("дата", "вологість", "тиск", "температура", "вітер"), columns);
    }

    @Test
    void storesBoundaryValuesAndRejectsInvalidMeasurements()
    {
        LocalDate date = LocalDate.of(2026, 9, 1);
        WeatherReadingRecord record = new WeatherReadingRecord(date, -5.5, 100, 0.01, 0);
        assertEquals(date, record.getDate());
        assertEquals(-5.5, record.getTemperature());
        assertEquals(100, record.getHumidity());
        assertEquals(0.01, record.getPressure());
        assertEquals(0, record.getWind());

        assertThrows(IllegalArgumentException.class, () -> new WeatherReadingRecord(date, Double.NaN, 50, 1000, 2));
        assertThrows(IllegalArgumentException.class, () -> new WeatherReadingRecord(date, 10, 101, 1000, 2));
        assertThrows(IllegalArgumentException.class, () -> new WeatherReadingRecord(date, 10, 50, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> new WeatherReadingRecord(date, 10, 50, 1000, -1));
        assertThrows(IllegalArgumentException.class, () -> new WeatherReadingRecord(date, 10, 50, Double.POSITIVE_INFINITY, 2));
    }

    @Test
    void missingDateIsRejectedAtConstruction()
    {
        assertThrows(IllegalArgumentException.class, () -> new WeatherReadingRecord(null, 10, 50, 1000, 2));
    }

    @Test
    void equalityUsesDateAndAllFourNumbers()
    {
        LocalDate date = LocalDate.of(2026, 9, 1);
        WeatherReadingRecord first = new WeatherReadingRecord(date, 10, 50, 1000, 2);
        WeatherReadingRecord second = new WeatherReadingRecord(date, 10, 50, 1000, 2);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(null));
        assertFalse(first.equals("інший тип"));

        for (WeatherReadingRecord different : List.of(
                new WeatherReadingRecord(date.plusDays(1), 10, 50, 1000, 2),
                new WeatherReadingRecord(date, 11, 50, 1000, 2),
                new WeatherReadingRecord(date, 10, 51, 1000, 2),
                new WeatherReadingRecord(date, 10, 50, 1001, 2),
                new WeatherReadingRecord(date, 10, 50, 1000, 3)))
        {
            assertFalse(first.equals(different));
        }
    }

    @Test
    void conversionPreservesValuesOfBothSubtypes()
    {
        for (WeatherReading reading : List.of(
                new DailyReading("2026-09-01", -5.5, 70, 1013, 7.5),
                new StormReading("2026-09-02", 25, 60, 1010, 20)))
        {
            WeatherReadingRecord record = WeatherReadingRecord.fromReading(reading);
            assertEquals(new WeatherReadingRecord(reading.getDate(), reading.getTemperature(),
                    reading.getHumidity(), reading.getPressure(), reading.getWind()), record);
        }
    }
}
