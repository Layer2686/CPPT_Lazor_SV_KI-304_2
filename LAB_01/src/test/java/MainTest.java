import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class MainTest {
    // The public report rounds to two decimals; allow half of the last displayed digit.
    private static final double REPORT_TOLERANCE = 0.0051;

    @TempDir
    Path tempDir;

    @Test
    void processesValidRecordsAndReportsInvalidOnes() throws IOException {
        String report = runCsv(String.join("\n",
                "2026-10-01;-3.25;50;1000;2",
                "2026-10-02;4;100;990;5.5",
                "2026-10-03;1;0;1010;0",
                "2026-10-04;1;помилка;1000;1",
                "2026-10-05;1;50;1000;-1") + "\n");
        assertStatistics(report, 3, -3.25, 50, 5.5);
        assertTrue(report.contains("Рядок 4: вологість не є числом"));
        assertTrue(report.contains("Рядок 5: вітер має бути скінченним невід’ємним числом"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validRows")
    void acceptsValidRowsAndBoundaryValues(String name, String row,
                                          double temperature, double humidity, double wind) throws IOException {
        String report = runCsv(row + "\n");
        assertStatistics(report, 1, temperature, humidity, wind);
        assertFalse(report.contains("Помилки:"));
    }

    static Stream<Arguments> validRows() {
        return Stream.of(
                Arguments.of("negative temperature", "2026-10-01;-12.75;40;1000;2", -12.75, 40, 2),
                Arguments.of("trimmed fields", " 2026-10-01 ; -2.75 ; 12.5 ; 1000 ; 0 ", -2.75, 12.5, 0),
                Arguments.of("humidity 0", "2026-10-01;4;0;1000;1", 4, 0, 1),
                Arguments.of("humidity 100", "2026-10-01;4;100;1000;1", 4, 100, 1),
                Arguments.of("positive pressure, zero wind", "2026-10-01;4;50;0.01;0", 4, 50, 0));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRows")
    void rejectsInvalidRowWithoutChangingStatisticsOrStoppingNextRow(
            String name, String invalidRow, String reason) throws IOException {
        String report = runCsv("2026-10-01;4;25;1000;1\n"
                + invalidRow + "\n2026-10-03;2;75;1000;3\n");
        assertStatistics(report, 2, 2, 50, 3);
        assertTrue(report.contains("Рядок 2: " + reason), report);
        assertEquals(1, report.lines().filter(line -> line.startsWith("Рядок ")).count());
    }

    static Stream<Arguments> invalidRows() {
        List<Arguments> cases = new ArrayList<>();
        cases.add(Arguments.of("empty line", "", "очікується 5 полів"));
        cases.add(Arguments.of("too few fields", "2026-10-02;1;50;1000", "очікується 5 полів"));
        cases.add(Arguments.of("too many fields", "2026-10-02;1;50;1000;2;extra", "очікується 5 полів"));
        cases.add(Arguments.of("empty date", ";1;50;1000;2", "Дата задана неправильно"));
        cases.add(Arguments.of("wrong date format", "2026/10/02;1;50;1000;2", "Дата задана неправильно"));
        cases.add(Arguments.of("incomplete date", "2026-1-02;1;50;1000;2", "Дата задана неправильно"));
        addInvalidValues(cases, 1, "температура", "", "помилка", "NaN", "Infinity", "-Infinity");
        addInvalidValues(cases, 2, "вологість", "", "помилка", "NaN", "Infinity", "-Infinity", "-0.01", "100.01");
        addInvalidValues(cases, 3, "тиск", "", "помилка", "NaN", "Infinity", "-Infinity", "0", "-1");
        addInvalidValues(cases, 4, "вітер", "", "помилка", "NaN", "Infinity", "-Infinity", "-0.01");
        return cases.stream();
    }

    private static void addInvalidValues(List<Arguments> cases, int field, String reason, String... values) {
        for (String value : values) {
            String[] fields = {"2026-10-02", "1", "50", "1000", "2"};
            fields[field] = value;
            cases.add(Arguments.of(reason + " / " + value, String.join(";", fields), reason));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("statisticCases")
    void calculatesExtremaAndFractionalMean(String name, String csv, int count,
                                          double temperature, double humidity, double wind) throws IOException {
        assertStatistics(runCsv(csv), count, temperature, humidity, wind);
    }

    static Stream<Arguments> statisticCases() {
        return Stream.of(
                Arguments.of("positive temperatures and fractional mean",
                        "2026-10-01;4;10;1000;1\n2026-10-02;6;20;1000;3\n2026-10-03;5;20;1000;2\n",
                        3, 4, 50.0 / 3, 3),
                Arguments.of("negative temperatures and zero winds",
                        "2026-10-01;-1;40;1000;0\n2026-10-02;-8.5;60;1000;0\n",
                        2, -8.5, 50, 0),
                Arguments.of("invalid pressure excludes extreme values",
                        "2026-10-01;-999;100;0;999\n2026-10-02;2;20;1000;2\n2026-10-03;4;31;1000;3\n",
                        2, 2, 25.5, 3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("emptyDataCases")
    void reportsNoDataWithoutInventingStatistics(String name, String csv, int expectedErrors) throws IOException {
        String report = runCsv(csv);
        assertTrue(report.contains("Немає даних"));
        assertTrue(report.contains("Кількість коректних записів: 0"));
        assertFalse(report.contains("Мінімальна температура:"));
        assertFalse(report.contains("Середній рівень вологості:"));
        assertFalse(report.contains("Максимальна швидкість вітру:"));
        assertEquals(expectedErrors, report.lines().filter(line -> line.startsWith("Рядок ")).count());
    }

    static Stream<Arguments> emptyDataCases() {
        return Stream.of(
                Arguments.of("empty file", "", 0),
                Arguments.of("blank line", "\n", 1),
                Arguments.of("only invalid records", "bad\n2026-10-02;1;помилка;1000;2\n", 2));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void acceptsBothOptionOrdersAndUkrainianPaths(boolean outputFirst) throws IOException {
        Path input = tempDir.resolve("вимірювання.csv");
        Path reportPath = tempDir.resolve("звіти/погода.txt");
        Files.writeString(input, "2026-10-01;-4.5;60;1000;1.2\n2026-10-02;1;помилка;1000;2\n",
                StandardCharsets.UTF_8);
        String[] args = outputFirst
                ? new String[]{"--output", reportPath.toString(), "--input", input.toString()}
                : new String[]{"--input", input.toString(), "--output", reportPath.toString()};
        CapturedOutput output = runMain(args);
        String report = readMatchingReport(reportPath, output);
        assertStatistics(report, 1, -4.5, 60, 1.2);
        assertTrue(report.contains("помилка"));
        assertEquals(1, output.stdout().lines().filter(line -> line.startsWith("Кількість коректних записів:")).count());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidArguments")
    void explainsInvalidArgumentsBeforeReadingInput(String name, String[] args, String message) {
        CapturedOutput output = runMain(args);
        assertEquals("", output.stdout());
        assertTrue(output.stderr().contains(message), output.stderr());
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(
                Arguments.of("unknown option", new String[]{"--unknown"}, "Невідомий параметр: --unknown"),
                Arguments.of("unknown option with value", new String[]{"--unknown", "file.csv"}, "Невідомий параметр"),
                Arguments.of("missing input value", new String[]{"--input"}, "Після --input бракує шляху"),
                Arguments.of("missing output value", new String[]{"--output"}, "Після --output бракує шляху"),
                Arguments.of("option instead of input path", new String[]{"--input", "--output"}, "Після --input бракує шляху"),
                Arguments.of("option instead of output path", new String[]{"--output", "--input"}, "Після --output бракує шляху"),
                Arguments.of("incomplete final option", new String[]{"--input", "unused.csv", "--output"}, "Після --output бракує шляху"));
    }

    @Test
    void helpDoesNotProcessWeatherData() {
        CapturedOutput output = runMain("--help");
        for (String option : List.of("--help", "--version", "--input", "--output")) {
            assertTrue(output.stdout().contains(option));
        }
        assertFalse(output.stdout().contains("Кількість коректних записів:"));
        assertEquals("", output.stderr());
    }

    @Test
    void versionFlagPrintsFilteredBuildMetadata() throws IOException {
        Properties buildInfo = new Properties();
        try (InputStream input = Main.class.getResourceAsStream("/build-info.properties")) {
            assertNotNull(input);
            buildInfo.load(input);
        }
        assertEquals("1.0.0", buildInfo.getProperty("app.version"));
        String buildNumber = buildInfo.getProperty("build.number");
        assertNotNull(buildNumber);
        assertTrue(buildNumber.matches("local|[1-9][0-9]*"));
        CapturedOutput output = runMain("--version");
        assertEquals("lab01 1.0.0 (build " + buildNumber + ")" + System.lineSeparator(), output.stdout());
        assertEquals("", output.stderr());
    }

    @Test
    void explainsMissingInputWithoutCreatingReport() {
        Path missing = tempDir.resolve("відсутній.csv");
        Path report = tempDir.resolve("report.txt");
        CapturedOutput output = runMain("--input", missing.toString(), "--output", report.toString());
        assertEquals("", output.stdout());
        assertTrue(output.stderr().contains("Не вдалося прочитати файл " + missing + ":"));
        assertFalse(Files.exists(report));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void explainsWriteFailureSeparatelyFromReadFailure(boolean parentIsFile) throws IOException {
        Path input = writeInput("2026-10-01;1;50;1000;2\n");
        Path blocked = tempDir.resolve("blocked");
        Path report;
        if (parentIsFile) {
            Files.writeString(blocked, "file", StandardCharsets.UTF_8);
            report = blocked.resolve("report.txt");
        } else {
            Files.createDirectory(blocked);
            report = blocked;
        }
        CapturedOutput output = runMain("--input", input.toString(), "--output", report.toString());
        assertStatistics(output.stdout(), 1, 1, 50, 2);
        assertTrue(output.stderr().contains("Неможливо записати звіт " + report + ":"));
        assertFalse(output.stderr().contains("Не вдалося прочитати файл"));
    }

    @Test
    void replacesPreviousReportRatherThanAppending() throws IOException {
        Path report = tempDir.resolve("out/report.txt");
        Files.createDirectories(report.getParent());
        Files.writeString(report, "OLD REPORT\n", StandardCharsets.UTF_8);
        String current = runCsv("2026-10-01;1;50;1000;2\n");
        assertStatistics(current, 1, 1, 50, 2);
        assertFalse(current.contains("OLD REPORT"));
    }

    @Test
    void usesDefaultPathsRelativeToWorkingDirectory() throws Exception {
        Path input = tempDir.resolve("data/input.csv");
        Files.createDirectories(input.getParent());
        Files.writeString(input, "2026-10-01;1;50;1000;2\n", StandardCharsets.UTF_8);
        String report = readMatchingReport(tempDir.resolve("out/report.txt"), runInSeparateJvm(List.of()));
        assertStatistics(report, 1, 1, 50, 2);
    }

    @Test
    void writesReportWithoutParentAndKeepsDecimalPointInGermanLocale() throws Exception {
        Path input = writeInput("2026-10-01;4;10;1000;1\n2026-10-02;6;20;1000;3\n2026-10-03;5;20;1000;2\n");
        CapturedOutput output = runInSeparateJvm(List.of("-Duser.language=de", "-Duser.country=DE"),
                "--input", input.toString(), "--output", "report.txt");
        String report = readMatchingReport(tempDir.resolve("report.txt"), output);
        assertStatistics(report, 3, 4, 50.0 / 3, 3);
        assertTrue(report.contains("Середній рівень вологості: 16.67 %"));
    }

    private Path writeInput(String csv) throws IOException {
        Path input = tempDir.resolve("weather.csv");
        Files.writeString(input, csv, StandardCharsets.UTF_8);
        return input;
    }

    private String runCsv(String csv) throws IOException {
        Path input = writeInput(csv);
        Path report = tempDir.resolve("out/report.txt");
        return readMatchingReport(report, runMain("--input", input.toString(), "--output", report.toString()));
    }

    private static String readMatchingReport(Path path, CapturedOutput output) throws IOException {
        assertEquals("", output.stderr());
        String report = Files.readString(path, StandardCharsets.UTF_8);
        assertFalse(report.isEmpty());
        assertTrue(output.stdout().endsWith(report), "Console must contain the same complete report as the file");
        return report;
    }

    private static void assertStatistics(String report, int count, double temperature, double humidity, double wind) {
        assertTrue(report.contains("Кількість коректних записів: " + count + System.lineSeparator()), report);
        assertEquals(temperature, reportedNumber(report, "Мінімальна температура", "°C"), REPORT_TOLERANCE);
        assertEquals(humidity, reportedNumber(report, "Середній рівень вологості", "%"), REPORT_TOLERANCE);
        assertEquals(wind, reportedNumber(report, "Максимальна швидкість вітру", "м/с"), REPORT_TOLERANCE);
    }

    private static double reportedNumber(String report, String label, String unit) {
        Matcher match = Pattern.compile("(?m)^" + Pattern.quote(label)
                + ": (-?\\d+\\.\\d{2}) " + Pattern.quote(unit) + "\\r?$").matcher(report);
        assertTrue(match.find(), "Missing value with two decimal places: " + label + "\n" + report);
        return Double.parseDouble(match.group(1));
    }

    private static CapturedOutput runMain(String... args) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream capturedErr = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            try {
                System.setOut(capturedOut);
                System.setErr(capturedErr);
                Main.main(args);
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }
        return new CapturedOutput(output.toString(StandardCharsets.UTF_8), errors.toString(StandardCharsets.UTF_8));
    }

    // A child JVM changes the real working directory and locale without changing global test state.
    private CapturedOutput runInSeparateJvm(List<String> jvmOptions, String... args) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Path classes = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> command = new ArrayList<>(List.of(java.toString(), "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8"));
        command.addAll(jvmOptions);
        command.addAll(List.of("-cp", classes.toString(), "Main"));
        command.addAll(List.of(args));
        Path stdout = tempDir.resolve("stdout.log");
        Path stderr = tempDir.resolve("stderr.log");
        Process process = new ProcessBuilder(command).directory(tempDir.toFile())
                .redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Child JVM timed out");
            String errors = Files.readString(stderr, StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), errors);
            return new CapturedOutput(Files.readString(stdout, StandardCharsets.UTF_8), errors);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private record CapturedOutput(String stdout, String stderr) {
    }
}
