package ua.lpnu.kzp.lab05;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class StorageCliTest
{
    @TempDir
    Path directory;

    private record Output(String out, String err)
    {
    }

    @Test
    void parsesNewPathsFlagsAndRejectsAmbiguousOrIncompleteArguments()
    {
        CommandLineOptions options = ArgumentParser.parse(new String[]{"--import-csv", "in.csv", "--export-csv",
                "out.csv", "--export-json", "out.json", "--round-trip", "--queries", "--danger"});
        assertEquals(Path.of("in.csv"), options.importCsvPath());
        assertEquals(Path.of("out.csv"), options.exportCsvPath());
        assertEquals(Path.of("out.json"), options.exportJsonPath());
        assertTrue(options.roundTrip());
        assertTrue(options.queries());
        assertTrue(options.danger());
        for (String option : List.of("--import-csv", "--export-csv", "--export-json"))
        {
            assertThrows(IllegalArgumentException.class, () -> ArgumentParser.parse(new String[]{option}));
            assertThrows(IllegalArgumentException.class, () -> ArgumentParser.parse(new String[]{option, "--danger"}));
        }
        assertThrows(IllegalArgumentException.class, () -> ArgumentParser.parse(new String[]{"--round-trip"}));
        assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]{"--input", "old.csv", "--import-csv", "new.csv"}));
    }

    @Test
    void realWorkflowExportsAndImportsWithoutChangingOldReports() throws Exception
    {
        Path input = directory.resolve("input.csv");
        Files.writeString(input, "2026-09-01;10;70;1013;7.5\n2026-09-01;25;60;1010;20\n");
        Path report = directory.resolve("report.txt");
        Path csv = directory.resolve("new/weather.csv");
        Path json = directory.resolve("new/weather.json");
        Output exported = run("--input", input.toString(), "--output", report.toString(),
                "--export-csv", csv.toString(), "--export-json", json.toString(), "--round-trip");
        assertEquals("", exported.err());
        assertTrue(exported.out().contains("Round-trip: true (записів: 2)"));
        assertTrue(Files.readString(json).contains("\"дата\": \"2026-09-01\""));
        String expected = Files.readString(report);
        Output imported = run("--import-csv", csv.toString(), "--output", report.toString(), "--queries", "--danger");
        assertEquals("", imported.err());
        assertEquals(expected, Files.readString(report));
        assertTrue(imported.out().contains("Максимальний індекс небезпеки: 40.00"));
        assertTrue(imported.out().contains("Результати потокових запитів:"));
        assertEquals(2, new WeatherCsvStorage().read(csv).size());
    }

    @Test
    void exportsOnlyValidRowsAndAcceptsEmptyInput() throws Exception
    {
        Path input = directory.resolve("input.csv");
        Path csv = directory.resolve("out.csv");
        Path report = directory.resolve("report.txt");
        Files.writeString(input, "2026-09-01;10;70;1013;7.5\n2026-09-02;10;помилка;1013;2\n");
        Output output = run("--input", input.toString(), "--output", report.toString(),
                "--export-csv", csv.toString(), "--round-trip");
        assertEquals("", output.err());
        assertTrue(output.out().contains("Round-trip: true (записів: 1)"));
        Files.writeString(input, "");
        output = run("--input", input.toString(), "--output", report.toString(),
                "--export-csv", csv.toString(), "--round-trip");
        assertEquals("", output.err());
        assertTrue(output.out().contains("Round-trip: true (записів: 0)"));
        assertEquals(List.of(), new WeatherCsvStorage().read(csv));
    }

    @Test
    void badImportMissingFileAndWriteFailureAreReported() throws Exception
    {
        Path bad = directory.resolve("bad.csv");
        Path report = directory.resolve("report.txt");
        Files.writeString(bad, "wrong,header\n");
        assertTrue(run("--import-csv", bad.toString(), "--output", report.toString()).err().contains("заголовок"));
        assertFalse(Files.exists(report));
        assertTrue(run("--import-csv", directory.resolve("missing.csv").toString(), "--output", report.toString())
                .err().contains("Не вдалося прочитати CSV"));
        Path old = directory.resolve("old.csv");
        Files.writeString(old, "2026-09-01;10;70;1013;7.5\n");
        assertTrue(run("--input", old.toString(), "--output", report.toString(), "--export-csv", directory.toString())
                .err().contains("Не вдалося записати CSV"));
    }

    @Test
    void collidingPathsAreRejectedBeforeAnyFileIsChanged() throws Exception
    {
        Path input = directory.resolve("input.csv");
        Files.writeString(input, "2026-09-01;10;70;1013;7.5\n");
        String before = Files.readString(input);
        Output output = run("--input", input.toString(), "--output", directory.resolve("report.txt").toString(),
                "--export-csv", input.toString());
        assertTrue(output.err().contains("мають відрізнятися"));
        assertEquals(before, Files.readString(input));
        assertFalse(Files.exists(directory.resolve("report.txt")));
        assertTrue(run("--input", input.toString(), "--output", directory.resolve("report.txt").toString(),
                "--export-csv", directory.resolve("same").toString(), "--export-json", directory.resolve("same").toString())
                .err().contains("мають відрізнятися"));
    }

    @Test
    void newOutputsThroughDirectoryAliasAreRejectedBeforeWriting() throws Exception
    {
        Path real = Files.createDirectory(directory.resolve("real"));
        Path alias = directory.resolve("alias");
        try
        {
            Files.createSymbolicLink(alias, real);
        }
        catch (java.io.IOException | UnsupportedOperationException exception)
        {
            // Звичайний Windows-користувач може не мати права створення symlink.
            if (!System.getProperty("os.name").startsWith("Windows"))
            {
                throw exception;
            }
            alias = real.resolve(".");
        }
        Path input = directory.resolve("input.csv");
        Files.writeString(input, "2026-09-01;10;70;1013;7.5\n");
        Path report = directory.resolve("report.txt");
        Output output = run("--input", input.toString(), "--output", report.toString(),
                "--export-csv", real.resolve("new/out").toString(),
                "--export-json", alias.resolve("new/out").toString());
        assertTrue(output.err().contains("мають відрізнятися"));
        assertFalse(Files.exists(report));
        assertFalse(Files.exists(real.resolve("new/out")));
    }

    private static Output run(String... args)
    {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (PrintStream stdout = new PrintStream(out, true, StandardCharsets.UTF_8);
                PrintStream stderr = new PrintStream(err, true, StandardCharsets.UTF_8))
        {
            System.setOut(stdout);
            System.setErr(stderr);
            Main.main(args);
        }
        finally
        {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        return new Output(out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }
}
