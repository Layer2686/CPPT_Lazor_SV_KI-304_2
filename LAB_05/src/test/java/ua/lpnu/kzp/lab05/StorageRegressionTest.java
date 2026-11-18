package ua.lpnu.kzp.lab05;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class StorageRegressionTest
{
    @TempDir
    Path directory;

    private enum Kind
    {
        DAILY, STORM
    }

    private static final class Label
    {
        @CsvColumn("Категорія") private final Kind kind;
        @CsvColumn("Текст") private final String text;

        Label(Kind kind, String text)
        {
            this.kind = kind;
            this.text = text;
        }

        static Label fromFields(List<String> fields) throws DataStorageException
        {
            if (fields.size() != 2)
            {
                throw new DataStorageException("Очікується категорія й текст");
            }
            try
            {
                return new Label(Kind.valueOf(fields.get(0)), fields.get(1));
            }
            catch (IllegalArgumentException exception)
            {
                throw new DataStorageException("Невідома категорія: " + fields.get(0), exception);
            }
        }
    }

    @Test
    void independentEnumAndTextTypeRoundTripsAndExplainsBadEnum() throws Exception
    {
        List<Label> labels = List.of(new Label(Kind.DAILY, "Карти, \"що говорять\""),
                new Label(Kind.STORM, "Перший рядок\nДругий рядок"));
        String csv = new CsvExporter<Label>().toCsv(Label.class, labels);
        List<List<String>> rows = CsvParser.parseDocument(csv);
        assertEquals(4, csv.lines().count());
        assertEquals(3, rows.size());
        for (int index = 0; index < labels.size(); index++)
        {
            Label restored = Label.fromFields(rows.get(index + 1));
            assertEquals(labels.get(index).kind, restored.kind);
            assertEquals(labels.get(index).text, restored.text);
        }
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> Label.fromFields(List.of("UNKNOWN", "текст")));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getMessage().contains("UNKNOWN"));
    }

    @Test
    void exportsAPathWithoutParentDirectory() throws Exception
    {
        Path path = Path.of("lab05-export-test-" + java.util.UUID.randomUUID() + ".csv");
        try
        {
            new CsvExporter<Label>().write(path, Label.class, List.of());
            assertEquals("Категорія,Текст\n", Files.readString(path));
        }
        finally
        {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void standardDangerAndQueriesStillMatchCapturedBaseline() throws Exception
    {
        for (String mode : List.of("standard", "danger", "queries"))
        {
            String data = switch (mode)
            {
                case "danger" -> "danger-example.csv";
                case "queries" -> "queries-example.csv";
                default -> "input.csv";
            };
            Path input = Path.of("data", data);
            if (!Files.exists(input))
            {
                input = Path.of("LAB_05").resolve(input);
            }
            Path output = directory.resolve(mode + ".txt");
            String[] args = mode.equals("standard")
                    ? new String[]{"--input", input.toString(), "--output", output.toString()}
                    : new String[]{"--input", input.toString(), "--output", output.toString(), "--" + mode};
            PrintStream originalOut = System.out;
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            try (PrintStream stream = new PrintStream(captured, true, StandardCharsets.UTF_8))
            {
                System.setOut(stream);
                Main.main(args);
            }
            finally
            {
                System.setOut(originalOut);
            }
            for (String suffix : List.of("console", "report"))
            {
                String resource = "/baseline/" + mode + "-" + suffix + ".txt";
                String expected;
                try (var source = getClass().getResourceAsStream(resource))
                {
                    assertNotNull(source);
                    expected = new String(source.readAllBytes(), StandardCharsets.UTF_8);
                }
                String actual = suffix.equals("console") ? captured.toString(StandardCharsets.UTF_8) : Files.readString(output);
                assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"), resource);
            }
        }
    }
}
