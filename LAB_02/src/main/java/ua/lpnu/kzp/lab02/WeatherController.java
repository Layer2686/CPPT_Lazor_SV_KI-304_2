package ua.lpnu.kzp.lab02;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Керує обробкою: файли → вимірювання → звіт → консоль і файл. */
public final class WeatherController {
    private final WeatherFileService files = new WeatherFileService();

    /** Створює контролер обробки метеоданих. */
    public WeatherController() {
    }

    /**
     * Виконує вибраний режим і пояснює помилки читання та запису окремо.
     * Помилка одного CSV-рядка не зупиняє обробку наступних.
     *
     * @param options параметри, перевірені парсером
     */
    public void run(CommandLineOptions options) {
        if (options.help()) {
            ApplicationInfo.printHelp();
            return;
        }
        if (options.version()) {
            ApplicationInfo.printVersion();
            return;
        }
        List<String> lines;
        try {
            lines = files.read(options.inputPath());
        } catch (IOException e) {
            System.err.println("Не вдалося прочитати файл " + options.inputPath() + ": " + e.getMessage());
            return;
        }

        List<WeatherReading> readings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                WeatherReading reading = WeatherReading.fromCsv(line);
                readings.add(reading);
                // Збережено консольне представлення записів із LAB_01.
                System.out.println("Рядок " + (i + 1) + ": дата " + reading.getDate()
                        + ", температура " + reading.getTemperature() + " °C, вологість "
                        + reading.getHumidity() + " %, тиск " + reading.getPressure()
                        + " гПа, вітер " + reading.getWind() + " м/с");
            } catch (IllegalArgumentException e) {
                if ("очікується 5 полів".equals(e.getMessage())) {
                    errors.add(String.format(Locale.ROOT,
                            "Рядок %d: %s", i + 1, e.getMessage()));
                } else {
                    errors.add(String.format(Locale.ROOT,
                            "Рядок %d: %s; вхід: %s", i + 1, e.getMessage(), line));
                }
            }
        }
        String reportText = WeatherReport.format(readings, errors);
        System.out.print(reportText);
        try {
            files.write(options.outputPath(), reportText);
        } catch (IOException e) {
            System.err.println("Неможливо записати звіт " + options.outputPath() + ": " + e.getMessage());
        }
    }
}
