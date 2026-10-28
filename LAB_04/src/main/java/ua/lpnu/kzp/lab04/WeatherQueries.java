package ua.lpnu.kzp.lab04;

import java.time.LocalDate;
import java.util.List;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;
import java.util.Comparator;

/** Потокові запити до вимірювань без зміни вхідного списку. */
public final class WeatherQueries
{
    private WeatherQueries()
    {
    }

    /**
     * Відбирає вимірювання з вітром строго вище порога, зберігаючи їхній порядок.
     *
     * @param readings початковий список коректних вимірювань
     * @param threshold поріг швидкості вітру, м/с
     * @return незмінний список відібраних вимірювань
     */
    public static List<WeatherReading> filterByWind(List<WeatherReading> readings, double threshold)
    {
        return readings.stream().filter(reading -> reading.getWind() > threshold).toList();
    }

    /**
     * Повертає календарні дати вимірювань у початковому порядку, включно з повтореннями.
     *
     * @param readings початковий список коректних вимірювань
     * @return незмінний список дат без текстових підписів
     */
    public static List<LocalDate> dates(List<WeatherReading> readings)
    {
        return readings.stream().map(reading -> reading.getDate()).toList();
    }

    /**
     * Обчислює середню температуру за роком і місяцем, враховуючи всі записи.
     *
     * @param readings початковий список коректних вимірювань
     * @return карта місяців і середніх температур; порожня для порожнього входу;
     *         порядок ключів не визначений
     */
    public static Map<YearMonth, Double> averageTemperatureByMonth(List<WeatherReading> readings)
    {
        return readings.stream().collect(Collectors.groupingBy
            (
                reading -> YearMonth.from(reading.getDate()),
                Collectors.averagingDouble(reading -> reading.getTemperature())
            ));
    }

    /**
     * Обчислює кількість, суму, мінімум, максимум і середнє температур усіх записів.
     *
     * @param readings початковий список коректних вимірювань
     * @return статистика температур; для порожнього входу count, sum і average — 0,
     *         min — +Infinity, max — -Infinity; перед виводом слід перевірити count
     */
    public static DoubleSummaryStatistics temperatureStatistics(List<WeatherReading> readings)
    {
        return readings.stream().collect(Collectors.summarizingDouble(reading -> reading.getTemperature()));
    }

    /**
     * Вибирає не більше limit записів за спаданням температури, потім за зростанням дати.
     *
     * @param readings початковий список коректних вимірювань
     * @param limit максимальна кількість записів; 0 повертає порожній список
     * @return незмінний відсортований список; завеликий limit залишає всі записи,
     *         повторення зберігаються
     * @throws IllegalArgumentException якщо limit від’ємний
     */
    public static List<WeatherReading> topByTemperature(List<WeatherReading> readings, int limit)
    {
        if (limit < 0)
        {
            throw new IllegalArgumentException("Кількість записів не може бути від’ємною");
        }
        Comparator<WeatherReading> order =
            Comparator.comparingDouble((WeatherReading reading) -> reading.getTemperature()).reversed()
                .thenComparing(reading -> reading.getDate());
        return readings.stream().sorted(order).limit(limit).toList();
    }
}
