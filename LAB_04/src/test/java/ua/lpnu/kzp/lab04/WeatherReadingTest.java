package ua.lpnu.kzp.lab04;

import java.time.LocalDate;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Прямі перевірки нової моделі без читання файлів і перехоплення консолі. */
@ResourceLock(Resources.LOCALE)
class WeatherReadingTest
{
    @Test
    void constructorStoresAllFields()
    {
        WeatherReading reading = new DailyReading("2026-09-01", -5.5, 70, 1013, 3.2);

        assertEquals(LocalDate.parse("2026-09-01"), reading.getDate());
        assertEquals(-5.5, reading.getTemperature(), 0.0001);
        assertEquals(70, reading.getHumidity(), 0.0001);
        assertEquals(1013, reading.getPressure(), 0.0001);
        assertEquals(3.2, reading.getWind(), 0.0001);
        assertEquals(new TemperatureHumidity(-5.5, 70), reading.getTemperatureHumidity());
    }

    @Test
    void constructorRejectsMissingOrMalformedDate()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading(null, 1, 50, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("   ", 1, 50, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026/09/01", 1, 50, 1000, 2));
    }

    @Test
    void constructorsValidateCalendarAndAcceptLeapDay()
    {
        WeatherReading leapDay = new DailyReading(" 2024-02-29 ", 1, 50, 1000, 2);
        assertEquals(LocalDate.of(2024, 2, 29), leapDay.getDate());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-02-29", 1, 50, 1000, 2));
        assertEquals("Дата задана неправильно", error.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-04-31", 1, 50, 1000, 20));
    }

    @Test
    void csvAndBuilderUseTheSameCalendarValidation()
    {
        WeatherReading leapDay = WeatherReading.fromCsv("2024-02-29;1;50;1000;20");
        assertEquals(LocalDate.of(2024, 2, 29), leapDay.getDate());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("2026-02-31;1;50;1000;20"));
        assertEquals("Дата задана неправильно", error.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.builder().date("2026-13-01")
                        .temperature(1).humidity(50).pressure(1000).wind(2).build());
    }

    @Test
    void constructorRejectsValuesOutsideDomain()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, -1, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 101, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, 0, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, -1, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, 1000, -1));
    }

    @Test
    void constructorRejectsNonFiniteNumbers()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", Double.NaN, 50, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", Double.POSITIVE_INFINITY, 50, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, Double.NaN, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, Double.POSITIVE_INFINITY, 1000, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, Double.NaN, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, Double.NEGATIVE_INFINITY, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, 1000, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 1, 50, 1000, Double.POSITIVE_INFINITY));
    }

    @Test
    void acceptsBoundaryValuesAndNegativeTemperature()
    {
        WeatherReading dry = new DailyReading("2026-09-01", -20, 0, 0.01, 0);
        WeatherReading humid = new DailyReading("2026-09-02", 0, 100, 1000, 0);

        assertEquals(-20, dry.getTemperature(), 0.0001);
        assertEquals(0, dry.getHumidity(), 0.0001);
        assertEquals(0, dry.getWind(), 0.0001);
        assertEquals(100, humid.getHumidity(), 0.0001);
    }

    @Test
    void fromCsvConvertsFieldsAndTrimsSpaces()
    {
        WeatherReading reading = WeatherReading.fromCsv(" 2026-09-01 ; -5.5 ; 70 ; 1013 ; 3.2 ");

        assertEquals(LocalDate.parse("2026-09-01"), reading.getDate());
        assertEquals(-5.5, reading.getTemperature(), 0.0001);
        assertEquals(70, reading.getHumidity(), 0.0001);
        assertEquals(1013, reading.getPressure(), 0.0001);
        assertEquals(3.2, reading.getWind(), 0.0001);
    }

    @Test
    void fromCsvRejectsNullWrongStructureAndWrongNumbers()
    {
        assertThrows(IllegalArgumentException.class, () -> WeatherReading.fromCsv(null));
        assertThrows(IllegalArgumentException.class, () -> WeatherReading.fromCsv(""));
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("2026-09-01;1;50;1000"));
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("2026-09-01;1;50;1000;2;extra"));
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("2026-09-01;1;50;1000;"));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("2026-09-01;1;помилка;1000;2"));
        assertEquals("вологість не є числом", error.getMessage());
    }

    @Test
    void fromCsvPreservesFirstLegacyErrorForSeveralWrongFields()
    {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("bad-date;помилка;101;0;-1"));

        assertEquals("вологість має бути від 0 до 100", error.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.fromCsv("2026-09-01;1;50;0;2"));
    }

    @Test
    void recordHasValueEqualityAndComponentAccessors()
    {
        TemperatureHumidity first = new TemperatureHumidity(-5.5, 70);
        TemperatureHumidity second = new TemperatureHumidity(-5.5, 70);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(-5.5, first.temperature(), 0.0001);
        assertEquals(70, first.humidity(), 0.0001);
        assertThrows(IllegalArgumentException.class, () -> new TemperatureHumidity(1, 101));
        assertThrows(IllegalArgumentException.class,
                () -> new TemperatureHumidity(Double.NaN, 50));
    }

    @Test
    void toStringUsesTwoDecimalsEvenWithGermanLocale()
    {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try
        {
            Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
            WeatherReading reading = new DailyReading("2026-09-01", -5.5, 70, 1013, 3.2);

            assertEquals("2026-09-01: -5.50 °C, 70.00 %, 1013.00 гПа, 3.20 м/с",
                    reading.toString());
        }
        finally
        {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }

    @Test
    void builderCreatesIndependentImmutableReadings()
    {
        WeatherReading.Builder builder = WeatherReading.builder()
                                                 .date("2026-09-01")
                                                 .temperature(-5.5)
                                                 .humidity(70)
                                                 .pressure(1013)
                                                 .wind(3.2);
        WeatherReading first = builder.build();
        WeatherReading second = builder.humidity(80).build();

        assertEquals(70, first.getHumidity(), 0.0001);
        assertEquals(80, second.getHumidity(), 0.0001);
        assertEquals(LocalDate.parse("2026-09-01"), first.getDate());
    }

    @Test
    void builderCannotBypassConstructorValidation()
    {
        assertThrows(IllegalArgumentException.class, () -> WeatherReading.builder().build());
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.builder().date("2026-09-01").temperature(1).humidity(101).pressure(1000).wind(2).build());
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.builder().date("2026-09-01").temperature(1).humidity(50).pressure(1000).build());
        assertThrows(IllegalArgumentException.class,
                () -> WeatherReading.builder().temperature(1).humidity(50).pressure(1000).wind(2).build());
    }
}
