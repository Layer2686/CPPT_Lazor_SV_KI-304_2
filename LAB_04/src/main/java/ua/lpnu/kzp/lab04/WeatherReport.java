package ua.lpnu.kzp.lab04;

import java.util.List;
import java.util.Locale;

/** Обчислює чотири показники за списком перевірених об’єктів і формує звіт. */
public final class WeatherReport
{
    private WeatherReport()
    {
    }

    /**
     * Зберігає порядок рядків, точність чисел та повідомлення звіту LAB_01.
     * Помилкові записи вже відсіяні до виклику цього методу.
     *
     * @param readings список коректних вимірювань
     * @param errors повідомлення про хибні CSV-рядки з їхніми номерами
     * @return спільний текст для консолі та UTF-8 файла
     */
    public static String format(List<WeatherReading> readings, List<String> errors)
    {
        double minTemperature = 0;
        double humiditySum = 0;
        double maxWind = 0;
        int validCount = 0;
        for (WeatherReading reading : readings)
        {
            TemperatureHumidity values = reading.getTemperatureHumidity();
            if (validCount == 0 || values.temperature() < minTemperature)
            {
                minTemperature = values.temperature();
            }
            humiditySum += values.humidity();
            if (validCount == 0 || reading.getWind() > maxWind)
            {
                maxWind = reading.getWind();
            }
            validCount++;
        }
        String reportText;
        if (validCount > 0)
        {
            double averageHumidity = humiditySum / validCount;
            reportText = String.format(Locale.ROOT,
                    "Мінімальна температура: %.2f °C%n"
                            + "Максимальна швидкість вітру: %.2f м/с%n"
                            + "Середній рівень вологості: %.2f %%%n"
                            + "Кількість коректних записів: %d%n",
                    minTemperature, maxWind, averageHumidity, validCount);
        }
        else
        {
            reportText = String.format(Locale.ROOT,
                    "Немає даних%nКількість коректних записів: %d%n", validCount);
        }
        if (!errors.isEmpty())
        {
            reportText += "Помилки: " + System.lineSeparator() + String.join(System.lineSeparator(), errors) + System.lineSeparator();
        }
        return reportText;
    }
    /**
     * Знаходить найбільший індекс через спільний контракт вимірювань.
     * @param readings список коректних вимірювань обох підтипів
     * @return найбільший індекс; для порожнього списку — 0
     */
    public static double maxDangerIndex(List<WeatherReading> readings)
    {
        double maxIndex = 0;
        for (WeatherReading reading : readings)
        {
            double currentIndex = reading.dangerIndex();
            if (currentIndex > maxIndex)
            {
                maxIndex = currentIndex;
            }
        }
        return maxIndex;
    }
    /**
     * Демонструє вичерпний pattern switch для дозволених підтипів.
     * @param reading непорожнє посилання на коректне вимірювання
     * @return назва звичайного або штормового вимірювання
     * @throws NullPointerException якщо reading дорівнює null
     */
    public static String describeReading(WeatherReading reading)
    {
        return switch (reading)
        {
            case DailyReading ignored -> "Звичайне вимірювання";
            case StormReading ignored -> "Штормове вимірювання";
        };
    }
    /**
     * Формує додатковий консольний аналіз без зміни стандартного звіту.
     * @param readings список коректних вимірювань
     * @return описи, індекси та максимум або повідомлення про відсутність даних
     */
    public static String formatDangerReport(List<WeatherReading> readings)
    {
        if (readings.isEmpty())
        {
            return "Немає даних для аналізу небезпеки" + System.lineSeparator();
        }
        StringBuilder text = new StringBuilder();
        text.append("Додатковий аналіз небезпеки:").append(System.lineSeparator());
        for (WeatherReading reading : readings)
        {
            text.append(String.format(Locale.ROOT, "%s: %s, індекс %.2f%n",reading.getDate(), describeReading(reading), reading.dangerIndex()));
        }
        text.append(String.format(Locale.ROOT, "Максимальний індекс небезпеки: %.2f%n", maxDangerIndex(readings)));
        return text.toString();
    }
}
