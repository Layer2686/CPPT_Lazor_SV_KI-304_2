package ua.lpnu.kzp.lab02;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Читає CSV і записує звіт у UTF-8; передає помилки вводу/виводу контролеру. */
public final class WeatherFileService
{
    /** Створює сервіс роботи з файлами. */
    public WeatherFileService()
    {
    }

    /**
     * Читає всі рядки, зберігаючи їхній порядок.
     *
     * @param inputPath шлях до CSV
     * @return рядки файла
     * @throws IOException якщо файл неможливо прочитати або декодувати
     */
    public List<String> read(Path inputPath) throws IOException
    {
        return Files.readAllLines(inputPath, StandardCharsets.UTF_8);
    }

    /**
     * Створює батьківські папки й замінює вміст звіту.
     * Працює також зі шляхом без батьківського каталогу.
     *
     * @param outputPath шлях до звіту
     * @param reportText готовий текст
     * @throws IOException якщо створення папок або запис неможливий
     */
    public void write(Path outputPath, String reportText) throws IOException
    {
        Path directory = outputPath.getParent();
        if (directory != null)
        {
            Files.createDirectories(directory);
        }
        Files.writeString(outputPath, reportText, StandardCharsets.UTF_8);
    }
}
