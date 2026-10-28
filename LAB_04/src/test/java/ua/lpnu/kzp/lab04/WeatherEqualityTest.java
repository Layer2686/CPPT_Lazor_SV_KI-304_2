package ua.lpnu.kzp.lab04;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Рівність вимірювань, контракт хешування та відсутність дублювання в HashSet. */
class WeatherEqualityTest
{
    @Test
    void equalityHandlesItselfNullAndOtherTypes()
    {
        WeatherReading daily = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading storm = new StormReading("2026-09-01", 10, 70, 1013, 20);

        assertTrue(daily.equals(daily));
        assertFalse(daily.equals(null));
        assertFalse(daily.equals("2026-09-01"));
        assertFalse(daily.equals(storm));
        assertFalse(storm.equals(daily));
    }

    @Test
    void dailyEqualityIsSymmetricTransitiveAndHasMatchingHashes()
    {
        WeatherReading first = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading second = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading third = WeatherReading.fromCsv("2026-09-01;10;70;1013;7.5");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(first.hashCode(), third.hashCode());
    }

    @Test
    void stormReadingsAlsoUseValueEquality()
    {
        WeatherReading first = new StormReading("2026-09-01", 10, 70, 1013, 15);
        WeatherReading second = WeatherReading.fromCsv("2026-09-01;10;70;1013;15");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void changingAnyMeasurementFieldMakesReadingsDifferent()
    {
        WeatherReading original = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);

        assertNotEquals(original, new DailyReading("2026-09-02", 10, 70, 1013, 7.5));
        assertNotEquals(original, new DailyReading("2026-09-01", 11, 70, 1013, 7.5));
        assertNotEquals(original, new DailyReading("2026-09-01", 10, 71, 1013, 7.5));
        assertNotEquals(original, new DailyReading("2026-09-01", 10, 70, 1014, 7.5));
        assertNotEquals(original, new DailyReading("2026-09-01", 10, 70, 1013, 8));
    }

    @Test
    void hashSetRemovesEqualReadingsButKeepsDifferentOnes()
    {
        Set<WeatherReading> unique = new HashSet<>();
        unique.add(new DailyReading("2026-09-01", 10, 70, 1013, 7.5));
        unique.add(new DailyReading("2026-09-01", 10, 70, 1013, 7.5));
        unique.add(new StormReading("2026-09-01", 10, 70, 1013, 15));
        unique.add(new StormReading("2026-09-01", 10, 70, 1013, 15));
        unique.add(new DailyReading("2026-09-01", 10, 71, 1013, 7.5));

        assertEquals(3, unique.size());
        assertTrue(unique.contains(WeatherReading.fromCsv("2026-09-01;10;70;1013;7.5")));
        assertTrue(unique.contains(WeatherReading.fromCsv("2026-09-01;10;70;1013;15")));
    }

    @Test
    void valueEqualityDoesNotRemoveDuplicateRecordsFromStandardReport()
    {
        WeatherReading first = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading second = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);

        String report = WeatherReport.format(List.of(first, second), List.of());

        assertTrue(report.contains("Кількість коректних записів: 2"));
    }
}
