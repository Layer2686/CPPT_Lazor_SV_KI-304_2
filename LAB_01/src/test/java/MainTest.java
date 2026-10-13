import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void processesValidRecordsAndReportsInvalidOnes(@TempDir Path tempDir) throws IOException {
        Path input = tempDir.resolve("weather.csv");
        Files.writeString(input, String.join("\n",
                "2026-10-01;-3.25;50;1000;2",
                "2026-10-02;4;100;990;5.5",
                "2026-10-03;1;0;1010;0",
                "2026-10-04;1;помилка;1000;1",
                "2026-10-05;1;50;1000;-1") + "\n", StandardCharsets.UTF_8);

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream capturedErr = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            try {
                System.setOut(capturedOut);
                System.setErr(capturedErr);
                Main.main(new String[] {"--input", input.toString()});
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }

        String actualOutput = output.toString(StandardCharsets.UTF_8);
        String actualErrors = errors.toString(StandardCharsets.UTF_8);
        assertTrue(actualOutput.contains("Мінімальна температура: -3.25 °C"));
        assertTrue(actualOutput.contains("Максимальна швидкість вітру: 5.50 м/с"));
        assertTrue(actualOutput.contains("Середній рівень вологості: 50.00 %"));
        assertTrue(actualOutput.contains("Кількість коректних записів: 3"));
        assertTrue(actualErrors.contains("Рядок 4: вологість не є числом"));
        assertTrue(actualErrors.contains("Рядок 5: вітер має бути скінченним невід’ємним числом"));
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

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            try {
                System.setOut(capturedOut);
                Main.main(new String[] {"--version"});
            } finally {
                System.setOut(originalOut);
            }
        }

        assertEquals("lab01 1.0.0 (build " + buildNumber + ")" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }
}
