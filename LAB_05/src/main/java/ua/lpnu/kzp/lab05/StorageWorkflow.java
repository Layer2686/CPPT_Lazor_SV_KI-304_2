package ua.lpnu.kzp.lab05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

/** Підключає CSV, JSON та перевірку round-trip до контролера. */
public final class StorageWorkflow
{
    /** Створює обробник операцій зберігання. */
    public StorageWorkflow()
    {
    }

    /**
     * Експортує лише коректні вимірювання; round-trip порівнює повні списки.
     * @param readings вимірювання зі сховища
     * @param options параметри запуску
     * @throws DataStorageException якщо експорт/читання/порівняння не вдалися
     */
    public void run(List<WeatherReading> readings, CommandLineOptions options) throws DataStorageException
    {
        List<WeatherReadingRecord> original = readings.stream().map(WeatherReadingRecord::fromReading).toList();
        if (options.exportCsvPath() != null)
        {
            Exporter<WeatherReadingRecord> exporter = new CsvExporter<>();
            exporter.write(options.exportCsvPath(), WeatherReadingRecord.class, original);
            System.out.println("CSV: " + options.exportCsvPath());
        }
        if (options.exportJsonPath() != null)
        {
            Exporter<WeatherReadingRecord> exporter = new JsonExporter<>();
            exporter.write(options.exportJsonPath(), WeatherReadingRecord.class, original);
            System.out.println("JSON: " + options.exportJsonPath());
        }
        if (options.roundTrip())
        {
            List<WeatherReadingRecord> restored = new WeatherCsvStorage().read(options.exportCsvPath());
            if (!original.equals(restored))
            {
                throw new DataStorageException("Round-trip: дані після читання відрізняються");
            }
            System.out.println("Round-trip: true (записів: " + restored.size() + ")");
        }
    }

    /**
     * Захищає джерело й результати від перезапису одне одним у нових режимах.
     * Перевіряє також наявні файлові псевдоніми (символічні/жорсткі посилання).
     * @param options параметри
     * @throws DataStorageException якщо шляхи збігаються або перевірка неможлива
     */
    public static void validatePaths(CommandLineOptions options) throws DataStorageException
    {
        if (options.importCsvPath() == null && options.exportCsvPath() == null && options.exportJsonPath() == null)
        {
            return;
        }
        List<Path> paths = new ArrayList<>();
        paths.add(options.importCsvPath() == null ? options.inputPath() : options.importCsvPath());
        paths.add(options.outputPath());
        if (options.exportCsvPath() != null)
        {
            paths.add(options.exportCsvPath());
        }
        if (options.exportJsonPath() != null)
        {
            paths.add(options.exportJsonPath());
        }
        for (int first = 0; first < paths.size(); first++)
        {
            for (int second = first + 1; second < paths.size(); second++)
            {
                Path left = paths.get(first);
                Path right = paths.get(second);
                try
                {
                    if (canonicalTarget(left).equals(canonicalTarget(right))
                            || Files.exists(left) && Files.exists(right) && Files.isSameFile(left, right))
                    {
                        throw new DataStorageException("Шляхи джерела та результатів мають відрізнятися: " + left);
                    }
                }
                catch (IOException | SecurityException exception)
                {
                    throw new DataStorageException("Не вдалося перевірити шляхи результатів", exception);
                }
            }
        }
    }

    // Відсутній кінцевий файл не заважає розкрити symlink його наявного предка.
    private static Path canonicalTarget(Path path) throws IOException
    {
        Path current = path.toAbsolutePath();
        List<Path> missing = new ArrayList<>();
        while (!Files.exists(current, LinkOption.NOFOLLOW_LINKS))
        {
            Path name = current.getFileName();
            Path parent = current.getParent();
            if (name == null || parent == null)
            {
                throw new IOException("Не знайдено доступного предка для " + path);
            }
            missing.add(name);
            current = parent;
        }
        Path resolved = current.toRealPath();
        for (int index = missing.size() - 1; index >= 0; index--)
        {
            resolved = resolved.resolve(missing.get(index));
        }
        return resolved.normalize();
    }

}
