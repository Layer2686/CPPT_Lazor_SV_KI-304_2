package ua.lpnu.kzp.lab04;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Перевіряє форматування, порядок груп і явні повідомлення порожнього результату. */
@ResourceLock(Resources.LOCALE)
class QueryReportTest
{
    @Test
    void printsChronologicalGroupsAndFirstMatchingDate()
    {
        List<WeatherReading> readings = List.of(
                new DailyReading("2026-10-01", -5, 70, 1013, 2),
                new DailyReading("2026-09-02", 10, 70, 1013, 3),
                new StormReading("2026-09-02", 20, 70, 1013, 20));
        String report = QueryReport.format(readings);

        assertTrue(report.contains(String.format(Locale.ROOT,
                "Середня температура за місяцями:%n2026-09: 15.00 °C%n2026-10: -5.00 °C%n")));
        assertTrue(report.contains("Пошук за датою 2026-09-02: 2026-09-02: 10.00 °C"));
        assertTrue(report.contains("Пошук за датою 2026-12-31: Не знайдено"));
        assertTrue(report.contains(String.format(Locale.ROOT,
                "2026-09:%n    DAILY: 10.00 °C%n    STORM: 20.00 °C%n2026-10:%n    DAILY: -5.00 °C%n")));
        assertFalse(report.contains("DoubleSummaryStatistics{"));
        assertTrue(report.endsWith(System.lineSeparator()));
    }

    @Test
    void emptyInputAndEmptyFilterHaveExplicitMessages()
    {
        assertEquals("Немає даних для потокових запитів" + System.lineSeparator(), QueryReport.format(List.of()));
        String report = QueryReport.format(List.of(new DailyReading("2026-09-01", 10, 70, 1013, 2)));

        assertTrue(report.contains("Немає відповідних вимірювань"));
        assertTrue(report.contains("Пошук за датою 2026-09-02: Не знайдено"));
        assertFalse(report.contains("Infinity"));
    }

    @Test
    void secondDemonstrationDateIsReallySearchedAndLocaleDoesNotChangeDecimals()
    {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try
        {
            Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
            String report = QueryReport.format(List.of(new StormReading("2026-12-31", 12.5, 70, 1013, 20)));

            assertTrue(report.contains("2026-12: 12.50 °C"));
            assertTrue(report.contains("Пошук за датою 2026-12-31: 2026-12-31: 12.50 °C"));
        }
        finally
        {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }
}
