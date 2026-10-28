package ua.lpnu.kzp.lab04;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Перевірки двох підтипів та вибору категорії на межі 15 м/с. */
class WeatherSubtypeTest
{
    @Test
    void subtypesProvideDifferentKindsAndDangerIndexesThroughBaseType()
    {
        WeatherReading daily = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading storm = new StormReading("2026-09-02", 10, 70, 1013, 20);

        assertEquals(WeatherKind.DAILY, daily.getKind());
        assertEquals(WeatherKind.STORM, storm.getKind());
        assertEquals(7.5, daily.dangerIndex(), 0.0001);
        assertEquals(40, storm.dangerIndex(), 0.0001);
    }

    @Test
    void constructorsEnforceTheirOwnWindRanges()
    {
        WeatherReading calm = new DailyReading("2026-09-01", 10, 70, 1013, 0);
        WeatherReading daily = new DailyReading("2026-09-01", 10, 70, 1013, 14.99);
        WeatherReading storm = new StormReading("2026-09-01", 10, 70, 1013, 15);

        assertEquals(0, calm.dangerIndex(), 0.0001);
        assertEquals(14.99, daily.dangerIndex(), 0.0001);
        assertEquals(30, storm.dangerIndex(), 0.0001);
        assertThrows(IllegalArgumentException.class,
                () -> new DailyReading("2026-09-01", 10, 70, 1013, 15));
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-09-01", 10, 70, 1013, 14.99));
    }

    @Test
    void stormConstructorAlsoRejectsInvalidCommonState()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading(null, 10, 70, 1013, 20));
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-09-01", 10, 101, 1013, 20));
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-09-01", 10, 70, 0, 20));
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-09-01", 10, 70, 1013, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-09-01", 10, 70, 1013, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new StormReading("2026-09-01", 10, 70, 1013, Double.POSITIVE_INFINITY));
    }

    @Test
    void csvFactorySelectsSubtypeAtThresholdAndPreservesFields()
    {
        WeatherReading daily = WeatherReading.fromCsv("2026-09-01;10;70;1013;14.99");
        WeatherReading storm = WeatherReading.fromCsv(" 2026-09-02 ; -5.5 ; 80 ; 1008 ; 15 ");

        assertInstanceOf(DailyReading.class, daily);
        assertInstanceOf(StormReading.class, storm);
        assertEquals(LocalDate.parse("2026-09-02"), storm.getDate());
        assertEquals(-5.5, storm.getTemperature(), 0.0001);
        assertEquals(80, storm.getHumidity(), 0.0001);
        assertEquals(1008, storm.getPressure(), 0.0001);
        assertEquals(15, storm.getWind(), 0.0001);
    }

    @Test
    void builderSelectsSubtypeWithoutChangingPreviousReading()
    {
        WeatherReading.Builder builder = WeatherReading.builder()
                                                 .date("2026-09-01")
                                                 .temperature(10)
                                                 .humidity(70)
                                                 .pressure(1013)
                                                 .wind(14.99);
        WeatherReading daily = builder.build();
        WeatherReading storm = builder.wind(15).build();

        assertInstanceOf(DailyReading.class, daily);
        assertInstanceOf(StormReading.class, storm);
        assertEquals(14.99, daily.getWind(), 0.0001);
        assertEquals(30, storm.dangerIndex(), 0.0001);
    }
}
