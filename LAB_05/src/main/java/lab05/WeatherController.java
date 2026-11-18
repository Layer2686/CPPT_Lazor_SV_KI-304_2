package lab05;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.nio.file.Path;

/** Керує обробкою: файли → вимірювання → звіт → консоль і файл. */
public final class WeatherController
{
    private final WeatherFileService files = new WeatherFileService();

    /** Створює контролер обробки метеоданих. */
    public WeatherController()
    {
    }

    /**
     * Виконує вибраний режим і пояснює помилки читання та запису окремо.
     * Помилка одного CSV-рядка не зупиняє обробку наступних.
     *
     * @param options параметри, перевірені парсером
     */
    public void run(CommandLineOptions options)
    {
        if (options.help())
        {
            ApplicationInfo.printHelp();
            return;
        }
        if (options.version())
        {
            ApplicationInfo.printVersion();
            return;
        }
        List<String> errors = new ArrayList<>();
        List<WeatherReading> readings;
        try
        {
            StorageWorkflow.validatePaths(options);
            if (options.importCsvPath() == null)
            {
                readings = readLegacy(options.inputPath(), errors);
            }
            else
            {
                readings = new WeatherCsvStorage().read(options.importCsvPath()).stream()
                        .map(WeatherReadingRecord::toReading).toList();
                for (int index = 0; index < readings.size(); index++)
                {
                    printReading(index + 1, readings.get(index));
                }
            }
        }
        catch (IOException exception)
        {
            System.err.println("Не вдалося прочитати файл " + options.inputPath() + ": " + exception.getMessage());
            return;
        }
        catch (DataStorageException exception)
        {
            System.err.println(exception.getMessage());
            return;
        }
        String reportText = WeatherReport.format(readings, errors);
        System.out.print(reportText);
        if (options.danger())
        {
            System.out.print(WeatherReport.formatDangerReport(readings));
        }
        if (options.queries())
        {
            System.out.print(QueryReport.format(readings));
        }
        try
        {
            files.write(options.outputPath(), reportText);
        }
        catch (IOException e)
        {
            System.err.println("Неможливо записати звіт " + options.outputPath() + ": " + e.getMessage());
            return;
        }
        try
        {
            new StorageWorkflow().run(readings, options);
        }
        catch (DataStorageException exception)
        {
            System.err.println(exception.getMessage());
        }
    }

    private List<WeatherReading> readLegacy(Path inputPath, List<String> errors) throws IOException
    {
        List<String> lines = files.read(inputPath);

        Repository<WeatherReading> repository = new Repository<>();
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            try
            {
                WeatherReading reading = WeatherReading.fromCsv(line);
                repository.add(reading);
                printReading(i + 1, reading);
            }
            catch (IllegalArgumentException e)
            {
                if ("очікується 5 полів".equals(e.getMessage()))
                {
                    errors.add(String.format(Locale.ROOT,
                            "Рядок %d: %s", i + 1, e.getMessage()));
                }
                else
                {
                    errors.add(String.format(Locale.ROOT,
                            "Рядок %d: %s; вхід: %s", i + 1, e.getMessage(), line));
                }
            }
        }
        return repository.all();
    }

    private static void printReading(int number, WeatherReading reading)
    {
        System.out.println("Рядок " + number + ": дата " + reading.getDate() + ", температура " + reading.getTemperature()
                + " °C, вологість " + reading.getHumidity() + " %, тиск " + reading.getPressure()
                + " гПа, вітер " + reading.getWind() + " м/с");
    }
}
