package ua.lpnu.kzp.lab05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Відтворюване порівняння повного CSV-експорту з кешем метаданих та без нього. */
public final class ExportBenchmark
{
    private static final int RECORDS = 10_000;
    private static final int BATCH_SIZE = 100;
    private static final int WARMUP = 3;
    private static final int REPEATS = 7;

    private ExportBenchmark()
    {
    }

    /**
     * Порівнює два режими на однакових 10 000 записах, включно з файловим I/O.
     * Порядок режимів чергується; результат — медіана, без вимоги прискорення.
     * @param args необов’язкова папка для двох наборів CSV
     * @throws DataStorageException якщо експорт неможливий
     * @throws IOException якщо неможливо порівняти результати
     */
    public static void main(String[] args) throws DataStorageException, IOException
    {
        if (args.length > 1)
        {
            throw new IllegalArgumentException("Вкажіть лише каталог результатів benchmark");
        }
        Path output = args.length == 0 ? Path.of("LAB_05/target/benchmark") : Path.of(args[0]);
        List<WeatherReadingRecord> records = new ArrayList<>();
        for (int index = 0; index < RECORDS; index++)
        {
            records.add(new WeatherReadingRecord(LocalDate.of(2026, 1, 1).plusDays(index % 365),
                    index % 60 - 20, index % 101, 1000 + index % 30, index % 30));
        }
        CsvExporter<WeatherReadingRecord> uncached = new CsvExporter<>(false);
        CsvExporter<WeatherReadingRecord> cached = new CsvExporter<>(true);
        for (int run = 0; run < WARMUP; run++)
        {
            export(uncached, records, output.resolve("uncached"));
            export(cached, records, output.resolve("cached"));
        }
        long[] withoutCache = new long[REPEATS];
        long[] withCache = new long[REPEATS];
        for (int run = 0; run < REPEATS; run++)
        {
            if (run % 2 == 0)
            {
                withoutCache[run] = export(uncached, records, output.resolve("uncached"));
                withCache[run] = export(cached, records, output.resolve("cached"));
            }
            else
            {
                withCache[run] = export(cached, records, output.resolve("cached"));
                withoutCache[run] = export(uncached, records, output.resolve("uncached"));
            }
        }
        for (int batch = 0; batch < RECORDS / BATCH_SIZE; batch++)
        {
            String file = "part-" + batch + ".csv";
            if (Files.mismatch(output.resolve("uncached").resolve(file), output.resolve("cached").resolve(file)) != -1)
            {
                throw new IllegalStateException("Кеш змінив CSV: " + file);
            }
        }
        System.out.printf(Locale.ROOT, "Java: %s; OS: %s %s%n", System.getProperty("java.version"),
                System.getProperty("os.name"), System.getProperty("os.arch"));
        System.out.printf("Records: %d; batches: %d; warmup: %d; repeats: %d%n",
                RECORDS, RECORDS / BATCH_SIZE, WARMUP, REPEATS);
        System.out.printf(Locale.ROOT, "Uncached median: %.3f ms%nCached median: %.3f ms%n",
                median(withoutCache) / 1_000_000.0, median(withCache) / 1_000_000.0);
        System.out.println("Uncached ns: " + Arrays.toString(withoutCache));
        System.out.println("Cached ns: " + Arrays.toString(withCache));
        System.out.println("Equal CSV files: " + RECORDS / BATCH_SIZE);
        System.out.println("Includes rendering and file I/O; timing is environment-dependent, not a test assertion.");
    }

    private static long export(CsvExporter<WeatherReadingRecord> exporter,
            List<WeatherReadingRecord> records, Path directory) throws DataStorageException
    {
        long start = System.nanoTime();
        for (int from = 0; from < records.size(); from += BATCH_SIZE)
        {
            exporter.write(directory.resolve("part-" + from / BATCH_SIZE + ".csv"), WeatherReadingRecord.class,
                    records.subList(from, Math.min(from + BATCH_SIZE, records.size())));
        }
        return System.nanoTime() - start;
    }

    private static long median(long[] values)
    {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }
}
