package ua.lpnu.kzp.lab02;

import java.util.List;
import java.util.Locale;

/** Обчислює чотири показники за списком перевірених об’єктів і формує звіт. */
public final class WeatherReport {
    private WeatherReport() {
    }

    /**
     * Зберігає порядок рядків, точність чисел та повідомлення звіту LAB_01.
     * Помилкові записи вже відсіяні до виклику цього методу.
     *
     * @param readings список коректних вимірювань
     * @param errors повідомлення про хибні CSV-рядки з їхніми номерами
     * @return спільний текст для консолі та UTF-8 файла
     */
    public static String format(List<WeatherReading> readings, List<String> errors) {
        double minTemperature = 0;
        double humiditySum = 0;
        double maxWind = 0;
        int validCount = 0;
        for (WeatherReading reading : readings) {
            TemperatureHumidity values = reading.getTemperatureHumidity();
            if (validCount == 0 || values.temperature() < minTemperature) {
                minTemperature = values.temperature();
            }
            humiditySum += values.humidity();
            if (validCount == 0 || reading.getWind() > maxWind) {
                maxWind = reading.getWind();
            }
            validCount++;
        }
        String reportText;
        if (validCount > 0) {
            double averageHumidity = humiditySum / validCount;
            reportText = String.format(Locale.ROOT,
                    "Мінімальна температура: %.2f °C%n"
                    + "Максимальна швидкість вітру: %.2f м/с%n"
                    + "Середній рівень вологості: %.2f %%%n"
                    + "Кількість коректних записів: %d%n",
                    minTemperature, maxWind, averageHumidity, validCount);
        } else {
            reportText = String.format(Locale.ROOT,
                    "Немає даних%nКількість коректних записів: %d%n", validCount);
        }
        if (!errors.isEmpty()) {
            reportText += "Помилки: " + System.lineSeparator()
                    + String.join(System.lineSeparator(), errors) + System.lineSeparator();
        }
        return reportText;
    }
}
