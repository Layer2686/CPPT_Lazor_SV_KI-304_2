package ua.lpnu.kzp.lab04;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void queriesDoNotChangeInputContentsOrOrder()
    {
        List<WeatherReading> readings = new ArrayList<>(List.of(
                new StormReading("2026-09-02", 10, 70, 1013, 20),
                new DailyReading("2026-09-01", 10, 70, 1013, 7.5)));
        List<WeatherReading> before = List.copyOf(readings);

        WeatherQueries.filterByWind(readings, 15);
        WeatherQueries.dates(readings);
        WeatherQueries.averageTemperatureByMonth(readings);

        assertEquals(before, readings);
    }
}
