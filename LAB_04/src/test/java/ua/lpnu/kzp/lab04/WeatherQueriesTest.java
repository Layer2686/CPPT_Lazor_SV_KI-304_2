package ua.lpnu.kzp.lab04;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Перевірки потокових запитів: межі відбору, порядок і збереження джерела. */
class WeatherQueriesTest
{
    @Test
    void filtersStrictlyAboveRequestedWindThreshold()
    {
        WeatherReading below = new DailyReading("2026-09-01", 10, 70, 1013, 14.99);
        WeatherReading boundary = new StormReading("2026-09-02", 10, 70, 1013, 15);
        WeatherReading above = new StormReading("2026-09-03", 10, 70, 1013, 15.01);
        List<WeatherReading> readings = List.of(boundary, above, below);

        assertEquals(List.of(above), WeatherQueries.filterByWind(readings, 15));
        assertEquals(readings, WeatherQueries.filterByWind(readings, 0));
        assertEquals(List.of(), WeatherQueries.filterByWind(readings, 100));
    }

    @Test
    void datesPreserveOrderAndRepeatedDatesOfBothSubtypes()
    {
        WeatherReading first = new DailyReading("2026-09-02", 10, 70, 1013, 7.5);
        WeatherReading second = new StormReading("2026-09-01", 10, 70, 1013, 20);
        WeatherReading third = new StormReading("2026-09-02", 10, 70, 1013, 15);

        assertEquals(List.of(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 2)),
                WeatherQueries.dates(List.of(first, second, third)));
    }

    @Test
    void filterAndDatesHandleEmptyInput()
    {
        assertEquals(List.of(), WeatherQueries.filterByWind(List.of(), 15));
        assertEquals(List.of(), WeatherQueries.dates(List.of()));
    }

    @Test
    void averagesTemperaturesByMonthAndYearIncludingRepeatedRecords()
    {
        WeatherReading first = new DailyReading("2026-09-01", 10, 70, 1013, 7.5);
        WeatherReading second = new StormReading("2026-09-02", 20, 70, 1013, 20);
        WeatherReading october = new DailyReading("2026-10-01", -5, 70, 1013, 2);
        WeatherReading nextYear = new StormReading("2027-09-01", 30, 70, 1013, 15);

        Map<YearMonth, Double> result = WeatherQueries.averageTemperatureByMonth(
                List.of(first, first, second, october, nextYear));

        assertEquals(3, result.size());
        assertEquals(40.0 / 3, result.get(YearMonth.of(2026, 9)), 0.0001);
        assertEquals(-5, result.get(YearMonth.of(2026, 10)), 0.0001);
        assertEquals(30, result.get(YearMonth.of(2027, 9)), 0.0001);
    }

    @Test
    void monthlyAverageHandlesEmptyInput()
    {
        assertEquals(Map.of(), WeatherQueries.averageTemperatureByMonth(List.of()));
    }

    @Test
    void summarizesTemperaturesIncludingNegativeAndRepeatedValues()
    {
        WeatherReading cold = new DailyReading("2026-09-01", -5, 70, 1013, 7.5);
        WeatherReading warm = new StormReading("2026-09-02", 20, 70, 1013, 20);

        DoubleSummaryStatistics result = WeatherQueries.temperatureStatistics(
                List.of(cold, warm, warm));

        assertEquals(3, result.getCount());
        assertEquals(35, result.getSum(), 0.0001);
        assertEquals(-5, result.getMin(), 0.0001);
        assertEquals(20, result.getMax(), 0.0001);
        assertEquals(35.0 / 3, result.getAverage(), 0.0001);
    }

    @Test
    void emptyTemperatureStatisticsExposeNoMeasurements()
    {
        DoubleSummaryStatistics result = WeatherQueries.temperatureStatistics(List.of());

        assertEquals(0, result.getCount());
        assertEquals(0, result.getSum());
        assertEquals(0, result.getAverage());
        assertEquals(Double.POSITIVE_INFINITY, result.getMin());
        assertEquals(Double.NEGATIVE_INFINITY, result.getMax());
    }

    @Test
    void topFiveSortsByDescendingTemperatureThenAscendingDate()
    {
        WeatherReading cold = new DailyReading("2026-09-01", -5, 70, 1013, 2);
        WeatherReading warmLate = new StormReading("2026-09-03", 25, 70, 1013, 20);
        WeatherReading warmEarly = new DailyReading("2026-09-02", 25, 70, 1013, 3);
        WeatherReading mild = new DailyReading("2026-09-04", 18, 70, 1013, 2);
        WeatherReading cool = new DailyReading("2026-09-05", 10, 70, 1013, 2);
        WeatherReading zero = new DailyReading("2026-09-06", 0, 70, 1013, 2);
        List<WeatherReading> readings = List.of(cold, warmLate, warmEarly, mild, cool, zero);

        assertEquals(List.of(warmEarly, warmLate, mild, cool, zero),
                WeatherQueries.topByTemperature(readings, 5));
        assertEquals(List.of(warmEarly, warmLate, mild, cool, zero, cold),
                WeatherQueries.topByTemperature(readings, 100));
    }

    @Test
    void topHandlesZeroEmptyInputAndRejectsNegativeLimit()
    {
        WeatherReading reading = new DailyReading("2026-09-01", 10, 70, 1013, 2);

        assertEquals(List.of(), WeatherQueries.topByTemperature(List.of(reading), 0));
        assertEquals(List.of(), WeatherQueries.topByTemperature(List.of(), 5));
        assertThrows(IllegalArgumentException.class,
                () -> WeatherQueries.topByTemperature(List.of(reading), -1));
        assertThrows(IllegalArgumentException.class,
                () -> WeatherQueries.topByTemperature(List.of(), -1));
    }

    @Test
    void topRetainsDuplicateMeasurements()
    {
        WeatherReading reading = new DailyReading("2026-09-01", 10, 70, 1013, 2);

        assertEquals(List.of(reading, reading),
                WeatherQueries.topByTemperature(List.of(reading, reading), 5));
    }

    @Test
    void findsEitherSubtypeAndReturnsFirstReadingForRepeatedDate()
    {
        WeatherReading first = new DailyReading("2026-09-02", 25, 70, 1013, 3);
        WeatherReading otherDate = new StormReading("2026-09-01", 20, 70, 1013, 20);
        WeatherReading repeatedDate = new StormReading("2026-09-02", 12, 50, 1008, 16);
        List<WeatherReading> readings = List.of(first, otherDate, repeatedDate);

        assertSame(first, WeatherQueries.findByDate(readings, LocalDate.of(2026, 9, 2)).orElseThrow());
        assertSame(otherDate, WeatherQueries.findByDate(readings, LocalDate.of(2026, 9, 1)).orElseThrow());
    }

    @Test
    void missingDateEmptyInputAndNullDateReturnEmptyOptional()
    {
        WeatherReading reading = new DailyReading("2026-09-01", 10, 70, 1013, 2);

        assertTrue(WeatherQueries.findByDate(List.of(reading), LocalDate.of(2026, 12, 31)).isEmpty());
        assertTrue(WeatherQueries.findByDate(List.of(), LocalDate.of(2026, 9, 1)).isEmpty());
        assertTrue(WeatherQueries.findByDate(List.of(reading), null).isEmpty());
    }

    @Test
    void groupsMonthlyTemperaturesByKindWithoutMixingYearsOrRepeatedRecords()
    {
        WeatherReading daily = new DailyReading("2026-09-01", 10, 70, 1013, 2);
        WeatherReading storm = new StormReading("2026-09-02", 20, 70, 1013, 20);
        WeatherReading colderStorm = new StormReading("2026-09-03", 8, 70, 1013, 15);
        WeatherReading october = new DailyReading("2026-10-01", -5, 70, 1013, 2);
        WeatherReading nextYear = new StormReading("2027-09-01", 30, 70, 1013, 20);

        Map<YearMonth, Map<WeatherKind, Double>> result =
                WeatherQueries.averageTemperatureByMonthAndKind(
                        List.of(daily, storm, storm, colderStorm, october, nextYear));

        assertEquals(3, result.size());
        assertEquals(Map.of(WeatherKind.DAILY, 10.0, WeatherKind.STORM, 16.0),
                result.get(YearMonth.of(2026, 9)));
        assertEquals(Map.of(WeatherKind.DAILY, -5.0), result.get(YearMonth.of(2026, 10)));
        assertEquals(Map.of(WeatherKind.STORM, 30.0), result.get(YearMonth.of(2027, 9)));
    }

    @Test
    void nestedMonthlyGroupingHandlesEmptyInput()
    {
        assertEquals(Map.of(), WeatherQueries.averageTemperatureByMonthAndKind(List.of()));
    }

    @Test
    void queriesDoNotChangeInputContentsOrOrder()
    {
        List<WeatherReading> readings = new ArrayList<>(List.of(
                new StormReading("2026-09-02", 10, 70, 1013, 20),
                new DailyReading("2026-09-01", 10, 70, 1013, 7.5)));
        List<WeatherReading> before = List.copyOf(readings);

        WeatherQueries.filterByWind(readings, 15);
        WeatherQueries.dates(readings);
        WeatherQueries.averageTemperatureByMonth(readings);
        WeatherQueries.temperatureStatistics(readings);
        WeatherQueries.topByTemperature(readings, 1);
        WeatherQueries.findByDate(readings, LocalDate.of(2026, 9, 1));
        WeatherQueries.averageTemperatureByMonthAndKind(readings);

        assertEquals(before, readings);
    }
}
