package ua.lpnu.kzp.lab03;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherFileServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void readsAndWritesUtf8WithAutomaticParentDirectories() throws IOException {
        WeatherFileService files = new WeatherFileService();
        Path output = tempDir.resolve("звіти/погода.txt");
        String text = "Мінімальна температура: -5.50 °C\nВологість: 70.00 %\n";

        files.write(output, text);

        assertEquals(text, Files.readString(output, StandardCharsets.UTF_8));
        assertEquals(List.of("Мінімальна температура: -5.50 °C", "Вологість: 70.00 %"),
                files.read(output));
    }

    @Test
    void replacesExistingContentsInsteadOfAppending() throws IOException {
        WeatherFileService files = new WeatherFileService();
        Path output = tempDir.resolve("report.txt");
        files.write(output, "Попередній довгий звіт");

        files.write(output, "Новий");

        assertEquals("Новий", Files.readString(output, StandardCharsets.UTF_8));
    }

    @Test
    void propagatesReadAndWriteFailuresToController() throws IOException {
        WeatherFileService files = new WeatherFileService();
        Path blocked = tempDir.resolve("blocked");
        Files.writeString(blocked, "це файл", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> files.read(tempDir.resolve("missing.csv")));
        assertThrows(IOException.class, () -> files.write(blocked.resolve("report.txt"), "Звіт"));
    }
}
