package ua.lpnu.kzp.lab03;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Прямі перевірки поліморфного аналізу та сумісного тексту звіту. */
class WeatherReportTest {
    @Test
    void findsMaximumDangerIndexRegardlessOfRecordOrder() {
        WeatherReading daily = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading storm = new StormReading("2026-09-02", 10, 70, 1013, 20);

        assertEquals(40, WeatherReport.maxDangerIndex(List.of(daily, storm)), 0.0001);
        assertEquals(40, WeatherReport.maxDangerIndex(List.of(storm, daily)), 0.0001);
    }

    @Test
    void emptyListHasZeroMaximumDangerIndex() {
        assertEquals(0, WeatherReport.maxDangerIndex(List.of()), 0.0001);
    }

    @Test
    void maximumAlsoWorksWithoutStormRecords() {
        WeatherReading calm = new DailyReading("2026-09-01", 10, 70, 1013, 0);
        WeatherReading windy = new DailyReading("2026-09-02", 10, 70, 1013, 14.99);

        assertEquals(0, WeatherReport.maxDangerIndex(List.of(calm)), 0.0001);
        assertEquals(14.99, WeatherReport.maxDangerIndex(List.of(windy, calm)), 0.0001);
    }

    @Test
    void standardReportKeepsWindSpeedInsteadOfDangerIndex() {
        WeatherReading storm = new StormReading("2026-09-01", 10, 70, 1013, 20);
        String expected = String.format(Locale.ROOT,
                "Мінімальна температура: 10.00 °C%n"
                + "Максимальна швидкість вітру: 20.00 м/с%n"
                + "Середній рівень вологості: 70.00 %%%n"
                + "Кількість коректних записів: 1%n");

        assertEquals(expected, WeatherReport.format(List.of(storm), List.of()));
    }
}
