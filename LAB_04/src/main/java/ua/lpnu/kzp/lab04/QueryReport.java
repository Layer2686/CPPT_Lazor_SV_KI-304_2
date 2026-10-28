package ua.lpnu.kzp.lab04;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.TreeMap;

/** Формує додатковий консольний звіт, використовуючи готові потокові запити. */
public final class QueryReport
{
    private static final double WIND_THRESHOLD = 15;
    private static final int TOP_LIMIT = 5;
    private static final LocalDate SEARCH_DATE = LocalDate.of(2026, 9, 2);
    private static final LocalDate SECOND_SEARCH_DATE = LocalDate.of(2026, 12, 31);

    private QueryReport()
    {
    }

    /**
     * Показує результати п’яти запитів, пошуку двох фіксованих дат і зірочки.
     * Друга дата відсутня в демонстраційному CSV, але теж шукається у вхідних даних.
     *
     * @param readings список коректних вимірювань
     * @return текст аналізу або повідомлення про відсутність даних
     */
    public static String format(List<WeatherReading> readings)
    {
        if (readings.isEmpty())
        {
            return "Немає даних для потокових запитів" + System.lineSeparator();
        }
        DoubleSummaryStatistics stats = WeatherQueries.temperatureStatistics(readings);
        StringBuilder query = new StringBuilder();
        query.append("Результати потокових запитів:").append(System.lineSeparator());
        query.append("Дати: ").append(WeatherQueries.dates(readings)).append(System.lineSeparator());
        query.append(String.format(Locale.ROOT,"Кількість: %d, сума: %.2f, мінімум: %.2f, максимум: %.2f, середнє: %.2f%n"
            ,stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage()));
        List<WeatherReading> windyReadings = WeatherQueries.filterByWind(readings, WIND_THRESHOLD);
        query.append("Вимірювання з вітром понад 15 м/с:").append(System.lineSeparator());
        if (windyReadings.isEmpty())
        {
            query.append("Немає відповідних вимірювань").append(System.lineSeparator());
        }
        for (WeatherReading reading : windyReadings)
        {
            query.append(reading).append(System.lineSeparator());
        }
        List<WeatherReading> temperatureTop = WeatherQueries.topByTemperature(readings, TOP_LIMIT);
        query.append("Top-5 за температурою:").append(System.lineSeparator());
        if (temperatureTop.isEmpty())
        {
            query.append("Немає відповідних вимірювань").append(System.lineSeparator());
        }
        for (WeatherReading reading : temperatureTop)
        {
            query.append(reading).append(System.lineSeparator());
        }

        query.append("Середня температура за місяцями:").append(System.lineSeparator());
        Map<YearMonth, Double> monthly = new TreeMap<>(WeatherQueries.averageTemperatureByMonth(readings));
        for (Map.Entry<YearMonth, Double> entry : monthly.entrySet())
        {
            query.append(String.format(Locale.ROOT, "%s: %.2f °C%n", entry.getKey(), entry.getValue()));
        }

        appendSearch(query, readings, SEARCH_DATE);
        appendSearch(query, readings, SECOND_SEARCH_DATE);

        query.append("Середня температура за місяцями та категоріями:").append(System.lineSeparator());
        Map<YearMonth, Map<WeatherKind, Double>> byMonthAndKind =
                new TreeMap<>(WeatherQueries.averageTemperatureByMonthAndKind(readings));
        for (Map.Entry<YearMonth, Map<WeatherKind, Double>> month : byMonthAndKind.entrySet())
        {
            query.append(month.getKey()).append(':').append(System.lineSeparator());
            for (WeatherKind kind : WeatherKind.values())
            {
                Double average = month.getValue().get(kind);
                if (average != null)
                {
                    query.append(String.format(Locale.ROOT, "    %s: %.2f °C%n", kind, average));
                }
            }
        }
        return query.toString();
    }

    // Optional перетворюється на опис; відсутній результат має явне повідомлення.
    private static void appendSearch(StringBuilder query, List<WeatherReading> readings, LocalDate date)
    {
        String result = WeatherQueries.findByDate(readings, date)
                .map(reading -> reading.toString())
                .orElse("Не знайдено");
        query.append("Пошук за датою ").append(date).append(": ")
                .append(result).append(System.lineSeparator());
    }
}
