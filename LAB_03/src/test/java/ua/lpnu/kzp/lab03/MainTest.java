package ua.lpnu.kzp.lab03;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Тести тимчасово змінюють вивід і локаль; JUnit не має змінювати їх одночасно.
@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
@ResourceLock(Resources.LOCALE)
class MainTest {
    @TempDir
    Path tempDir; // JUnit створює й прибирає окрему тимчасову папку для кожного тесту.

    String consoleText;
    String errorText;

    @Test
    void calculatesFourStatistics() throws IOException {
        String report = runCsv(
                "2026-10-01;4;10;1000;1\n"
                + "2026-10-02;-3.25;20;1000;5.5\n"
                + "2026-10-03;6;20;1000;2\n");

        assertTrue(report.contains("Кількість коректних записів: 3"));
        assertTrue(report.contains("Мінімальна температура: -3.25 °C"));
        assertTrue(report.contains("Максимальна швидкість вітру: 5.50 м/с"));
        assertTrue(report.contains("Середній рівень вологості: 16.67 %"));

        // Третій рядок звіту містить вологість. Беремо число після ": " і до пробілу.
        String humidityLine = report.split(System.lineSeparator())[2];
        double actualAverage = Double.parseDouble(humidityLine.split(": ")[1].split(" ")[0]);
        // Середнє (10 + 20 + 20) / 3 округлено у звіті до двох знаків.
        assertEquals(50.0 / 3, actualAverage, 0.0051);
    }

    @Test
    void acceptsSpacesAndBoundaryValues() throws IOException {
        String report = runCsv(
                " 2026-10-01 ; -2.75 ; 0 ; 0.01 ; 0 \n"
                + "2026-10-02;4;100;1000;3\n");

        assertTrue(report.contains("Кількість коректних записів: 2"));
        assertTrue(report.contains("Мінімальна температура: -2.75 °C"));
        assertTrue(report.contains("Середній рівень вологості: 50.00 %"));
        assertTrue(report.contains("Максимальна швидкість вітру: 3.00 м/с"));
        assertFalse(report.contains("Помилки:"));
    }

    @Test
    void rejectsWrongStructureAndEmptyFields() throws IOException {
        String report = runCsv(
                "\n"                                    // 1: порожній рядок
                + "2026-10-02;1;50;1000\n"               // 2: лише чотири поля
                + "2026-10-03;1;50;1000;2;extra\n"       // 3: зайве поле
                + "2026-10-04;1;50;1000;\n"             // 4: порожній вітер
                + ";1;50;1000;2\n"                      // 5: порожня дата
                + "2026/10/06;1;50;1000;2\n"            // 6: неправильний формат дати
                + "2026-10-07;1;;1000;2\n"              // 7: порожня вологість
                + "2026-10-08;1;50;1000;2\n");          // 8: коректний запис після помилок

        assertTrue(report.contains("Кількість коректних записів: 1"));
        assertTrue(report.contains("Рядок 1: очікується 5 полів"));
        assertTrue(report.contains("Рядок 2: очікується 5 полів"));
        assertTrue(report.contains("Рядок 3: очікується 5 полів"));
        assertTrue(report.contains("Рядок 4: вітер не є числом"));
        assertTrue(report.contains("Рядок 5: Дата задана неправильно"));
        assertTrue(report.contains("Рядок 6: Дата задана неправильно"));
        assertTrue(report.contains("Рядок 7: вологість не є числом"));
    }

    @Test
    void rejectsWrongNumbersAndNonFiniteValues() throws IOException {
        String report = runCsv(
                "2026-10-01;помилка;50;1000;2\n"
                + "2026-10-02;1;помилка;1000;2\n"
                + "2026-10-03;1;50;помилка;2\n"
                + "2026-10-04;1;50;1000;помилка\n"
                + "2026-10-05;NaN;50;1000;2\n"
                + "2026-10-06;1;Infinity;1000;2\n"
                + "2026-10-07;1;50;-Infinity;2\n"
                + "2026-10-08;1;50;1000;NaN\n");

        assertTrue(report.contains("Кількість коректних записів: 0"));
        assertTrue(report.contains("Рядок 1: температура не є числом"));
        assertTrue(report.contains("Рядок 2: вологість не є числом"));
        assertTrue(report.contains("Рядок 3: тиск не є числом"));
        assertTrue(report.contains("Рядок 4: вітер не є числом"));
        assertTrue(report.contains("Рядок 5: температура має бути скінченним числом"));
        assertTrue(report.contains("Рядок 6: вологість має бути від 0 до 100"));
        assertTrue(report.contains("Рядок 7: тиск має бути додатним скінченним числом"));
        assertTrue(report.contains("Рядок 8: вітер має бути скінченним невід’ємним числом"));
        assertTrue(report.contains("Немає даних"));
    }

    @Test
    void invalidRecordsDoNotChangeStatistics() throws IOException {
        String report = runCsv(
                "2026-10-01;4;25;1000;1\n"             // Коректний.
                + "2026-10-02;-999;-1;1000;999\n"       // Вологість нижче 0.
                + "2026-10-03;-999;101;1000;999\n"      // Вологість вище 100.
                + "2026-10-04;-999;100;1000;-1\n"       // Від'ємний вітер.
                + "2026-10-05;-999;100;0;999\n"         // Нульовий тиск.
                + "2026-10-06;-999;100;-1;999\n"        // Від'ємний тиск.
                + "2026-10-07;2;75;1000;3\n");         // Коректний після помилок.

        assertTrue(report.contains("Кількість коректних записів: 2"));
        assertTrue(report.contains("Мінімальна температура: 2.00 °C"));
        assertTrue(report.contains("Середній рівень вологості: 50.00 %"));
        assertTrue(report.contains("Максимальна швидкість вітру: 3.00 м/с"));
        assertTrue(report.contains("Рядок 2: вологість"));
        assertTrue(report.contains("Рядок 3: вологість"));
        assertTrue(report.contains("Рядок 4: вітер"));
        assertTrue(report.contains("Рядок 5: тиск"));
        assertTrue(report.contains("Рядок 6: тиск"));
    }

    @Test
    void emptyFileHasNoData() throws IOException {
        String report = runCsv("");
        assertTrue(report.contains("Кількість коректних записів: 0"));
        assertTrue(report.contains("Немає даних"));
        assertFalse(report.contains("Мінімальна температура:"));
        assertFalse(report.contains("Середній рівень вологості:"));
        assertFalse(report.contains("Максимальна швидкість вітру:"));
        assertFalse(report.contains("Помилки:"));
    }

    @Test
    void helpAndVersionDoNotReadInput() throws IOException {
        runMain("--help");
        assertTrue(consoleText.contains("--input"));
        assertTrue(consoleText.contains("--output"));
        assertFalse(consoleText.contains("Кількість коректних записів:"));
        assertEquals("", errorText);

        Properties buildInfo = new Properties();
        try (InputStream input = Main.class.getResourceAsStream("/build-info.properties")) {
            buildInfo.load(input);
        }
        runMain("--version");
        assertEquals("lab03 " + buildInfo.getProperty("app.version") + " (build "
                + buildInfo.getProperty("build.number") + ")" + System.lineSeparator(), consoleText);
        assertEquals("", errorText);
    }

    @Test
    void invalidArgumentsAreExplained() {
        runMain("--unknown");
        assertTrue(errorText.contains("Невідомий параметр: --unknown"));
        assertEquals("", consoleText);
        runMain("--input");
        assertTrue(errorText.contains("Після --input бракує шляху"));
        runMain("--output");
        assertTrue(errorText.contains("Після --output бракує шляху"));
        runMain("--input", "--output");
        assertTrue(errorText.contains("Після --input бракує шляху"));
    }

    @Test
    void ukrainianPathsAndReversedOptionsWork() throws IOException {
        Path input = tempDir.resolve("вимірювання.csv");
        Path output = tempDir.resolve("звіти/погода.txt");
        Files.writeString(input, "2026-10-01;1;помилка;1000;2\n", StandardCharsets.UTF_8);
        runMain("--output", output.toString(), "--input", input.toString());
        String report = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals(report, consoleText); // Тут немає коректних рядків перед підсумком.
        assertTrue(report.contains("вологість не є числом"));
        assertTrue(report.contains("помилка"));
        assertEquals("", errorText);
    }

    @Test
    void missingInputIsExplained() {
        Path missing = tempDir.resolve("missing.csv");
        Path output = tempDir.resolve("report.txt");
        runMain("--input", missing.toString(), "--output", output.toString());
        assertTrue(errorText.contains("Не вдалося прочитати файл " + missing + ":"));
        assertEquals("", consoleText);
        assertFalse(Files.exists(output));
    }

    @Test
    void writeFailureIsExplained() throws IOException {
        Path input = tempDir.resolve("input.csv");
        Path blocked = tempDir.resolve("blocked");
        Files.writeString(input, "2026-10-01;1;50;1000;2\n", StandardCharsets.UTF_8);
        Files.writeString(blocked, "Це файл, а не папка", StandardCharsets.UTF_8);
        Path output = blocked.resolve("report.txt");
        runMain("--input", input.toString(), "--output", output.toString());
        assertTrue(errorText.contains("Неможливо записати звіт " + output + ":"));
        assertFalse(errorText.contains("Не вдалося прочитати файл"));
    }

    @Test
    void germanLocaleStillUsesDecimalPoint() throws IOException {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
            String report = runCsv("2026-10-01;-2.5;12.5;1000;1.5\n");
            assertTrue(report.contains("Мінімальна температура: -2.50 °C"));
            assertTrue(report.contains("Середній рівень вологості: 12.50 %"));
            assertTrue(report.contains("Максимальна швидкість вітру: 1.50 м/с"));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }

    // Готує CSV, запускає програму й повертає звіт. Файли живуть лише в tempDir.
    private String runCsv(String csv) throws IOException {
        Path input = tempDir.resolve("input.csv");
        Path output = tempDir.resolve("out/report.txt");
        Files.writeString(input, csv, StandardCharsets.UTF_8);
        runMain("--input", input.toString(), "--output", output.toString());
        String report = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals("", errorText);
        assertTrue(consoleText.endsWith(report)); // Той самий підсумок у консолі й файлі.
        return report;
    }

    // Перенаправляє stdout/stderr у пам'ять, щоб тести могли перевірити надрукований текст.
    private void runMain(String... args) {
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
        consoleText = output.toString(StandardCharsets.UTF_8);
        errorText = errors.toString(StandardCharsets.UTF_8);
    }
}
