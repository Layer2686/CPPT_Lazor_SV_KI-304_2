package lab05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class WeatherCsvStorageTest
{
    @TempDir
    Path directory;

    @Test
    void roundTripKeepsBothSubtypesAllValuesOrderAndDuplicateDates() throws Exception
    {
        List<WeatherReading> readings = List.of(
                new DailyReading("2026-09-01", -5.5, 0, 1000, 0),
                new StormReading("2026-09-01", 25, 100, 1013, 20),
                new DailyReading("2026-09-01", -5.5, 0, 1000, 0));
        List<WeatherReadingRecord> original = readings.stream().map(WeatherReadingRecord::fromReading).toList();
        Path path = directory.resolve("weather.csv");
        new CsvExporter<WeatherReadingRecord>().write(path, WeatherReadingRecord.class, original);
        List<WeatherReadingRecord> restored = new WeatherCsvStorage().read(path);
        assertEquals(original, restored);
        assertEquals(original.hashCode(), restored.hashCode());
        assertEquals(readings, restored.stream().map(WeatherReadingRecord::toReading).toList());
        assertThrows(UnsupportedOperationException.class, () -> restored.clear());
    }

    @Test
    void emptyFileAndHeaderOnlyAreEmptyAndSingleRecordCanBeParsed() throws Exception
    {
        Path path = directory.resolve("empty.csv");
        Files.writeString(path, "");
        assertEquals(List.of(), new WeatherCsvStorage().read(path));
        new CsvExporter<WeatherReadingRecord>().write(path, WeatherReadingRecord.class, List.of());
        assertEquals(List.of(), new WeatherCsvStorage().read(path));
        assertEquals(new WeatherReadingRecord(LocalDate.of(2024, 2, 29), 10, 50, 1000, 2),
                WeatherReadingRecord.fromCsv("2024-02-29,50,1000,10,2"));
    }

    @Test
    void badHeaderFieldsDateNumberAndInvariantsAreExplainedWithCause() throws Exception
    {
        Path path = directory.resolve("bad.csv");
        Files.writeString(path, "не той заголовок\n");
        assertTrue(assertThrows(DataStorageException.class, () -> new WeatherCsvStorage().read(path))
                .getMessage().contains("заголовок"));
        assertThrows(DataStorageException.class, () -> WeatherReadingRecord.fromCsv("2026-09-01,50"));
        assertThrows(DataStorageException.class, () -> WeatherReadingRecord.fromCsv(""));
        assertThrows(DataStorageException.class, () -> WeatherReadingRecord.fromCsv("a\nb"));
        for (String bad : List.of("2026-09-01,101,1000,10,2", "2026-09-01,50,0,10,2",
                "2026-09-01,50,1000,NaN,2", "2026-09-01,50,1000,10,-1"))
        {
            assertInstanceOf(IllegalArgumentException.class,
                    assertThrows(DataStorageException.class, () -> WeatherReadingRecord.fromCsv(bad)).getCause());
        }
        assertInstanceOf(DateTimeParseException.class, assertThrows(DataStorageException.class,
                () -> WeatherReadingRecord.fromCsv("2026-02-31,50,1000,10,2")).getCause());
        Files.writeString(path, String.join(",", WeatherReadingRecord.csvHeader()) + "\n2026-09-01,помилка,1000,10,2\n");
        DataStorageException number = assertThrows(DataStorageException.class, () -> new WeatherCsvStorage().read(path));
        assertTrue(number.getMessage().contains("Запис 2"));
        assertTrue(number.getMessage().contains("вологість"));
        assertInstanceOf(NumberFormatException.class, number.getCause());
    }

    @Test
    void missingFilePreservesFilesystemCause()
    {
        assertInstanceOf(IOException.class, assertThrows(DataStorageException.class,
                () -> new WeatherCsvStorage().read(directory.resolve("missing.csv"))).getCause());
    }
}
