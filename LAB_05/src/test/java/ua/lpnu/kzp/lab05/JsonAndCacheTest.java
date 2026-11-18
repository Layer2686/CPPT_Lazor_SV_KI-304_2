package ua.lpnu.kzp.lab05;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class JsonAndCacheTest
{
    @TempDir
    Path directory;

    private static final class Mixed
    {
        @CsvColumn("Текст\"") private final String text = "Львів, \"цитата\"\n\r\t\\\u0001😀";
        @CsvColumn("Активний") private final boolean active = true;
        @CsvColumn("Число") private final double number;
        @CsvColumn("Порожнє") private final String optional = null;

        Mixed(double number)
        {
            this.number = number;
        }
    }

    private static final class Duplicate
    {
        @CsvColumn("Однакове") private final int first = 1;
        @CsvColumn("Однакове") private final int second = 2;
    }

    private static final class Nested
    {
        @CsvColumn("Об’єкт") private final Object child = new Object();
    }

    @Test
    void jsonPreservesTypesNullAndEscapesAllSpecialCharacters() throws Exception
    {
        Exporter<Mixed> exporter = new JsonExporter<>();
        Path path = directory.resolve("nested/mixed.json");
        exporter.write(path, Mixed.class, List.of(new Mixed(12.5)));
        String json = Files.readString(path);
        assertEquals("[\n  {\"Активний\": true, \"Число\": 12.5, \"Порожнє\": null, "
                + "\"Текст\\\"\": \"Львів, \\\"цитата\\\"\\n\\r\\t\\\\\\u0001\\ud83d\\ude00\"}\n]\n", json);
        exporter.write(path, Mixed.class, List.of());
        assertEquals("[]\n", Files.readString(path));
    }

    @Test
    void jsonAndCsvShareWeatherColumnsAndRejectInvalidInputs() throws Exception
    {
        WeatherReadingRecord record = new WeatherReadingRecord(LocalDate.of(2026, 9, 1), 10, 70, 1013, 7.5);
        String json = new JsonExporter<WeatherReadingRecord>().toJson(WeatherReadingRecord.class, List.of(record));
        assertEquals("[\n  {\"дата\": \"2026-09-01\", \"вологість\": 70.0, \"тиск\": 1013.0, \"температура\": 10.0, \"вітер\": 7.5}\n]\n", json);
        assertThrows(DataStorageException.class, () -> new JsonExporter<Mixed>().toJson(Mixed.class, null));
        assertThrows(DataStorageException.class,
                () -> new JsonExporter<Mixed>().toJson(Mixed.class, Arrays.asList((Mixed) null)));
        assertThrows(DataStorageException.class,
                () -> new JsonExporter<Mixed>().toJson(Mixed.class, List.of(new Mixed(Double.NaN))));
        assertThrows(DataStorageException.class,
                () -> new JsonExporter<Nested>().toJson(Nested.class, List.of(new Nested())));
        assertThrows(DataStorageException.class,
                () -> new JsonExporter<Duplicate>().toJson(Duplicate.class, List.of()));
        assertInstanceOf(IOException.class, assertThrows(DataStorageException.class,
                () -> new JsonExporter<Mixed>().write(directory, Mixed.class, List.of())).getCause());
    }

    @Test
    void cacheReusesOnlyMetadataAndDoesNotChangeCsvOrKeepOldValues() throws Exception
    {
        assertSame(ColumnMetadata.columns(Mixed.class, true), ColumnMetadata.columns(Mixed.class, true));
        assertNotSame(ColumnMetadata.columns(Mixed.class, false), ColumnMetadata.columns(Mixed.class, false));
        CsvExporter<Mixed> cached = new CsvExporter<>(true);
        CsvExporter<Mixed> uncached = new CsvExporter<>(false);
        for (double number : List.of(1.0, -0.0, 100.5))
        {
            List<Mixed> records = List.of(new Mixed(number));
            assertEquals(uncached.toCsv(Mixed.class, records), cached.toCsv(Mixed.class, records));
            assertTrue(cached.toCsv(Mixed.class, records).contains(Double.toString(number)));
        }
        assertThrows(DataStorageException.class,
                () -> new CsvExporter<Duplicate>().toCsv(Duplicate.class, List.of()));
    }
}
