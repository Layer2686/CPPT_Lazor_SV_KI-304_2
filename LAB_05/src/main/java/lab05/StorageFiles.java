package lab05;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Файлова межа сховища: UTF-8, контекст і незмінена першопричина. */
final class StorageFiles
{
    private StorageFiles()
    {
    }

    static void write(Path path, String text, String format) throws DataStorageException
    {
        if (path == null)
        {
            throw new DataStorageException("Шлях до " + format + " не може бути null");
        }
        try
        {
            Path parent = path.getParent();
            if (parent != null)
            {
                Files.createDirectories(parent);
            }
            Files.writeString(path, text, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
        catch (IOException | SecurityException exception)
        {
            throw new DataStorageException("Не вдалося записати " + format + ": " + path, exception);
        }
    }

    static String read(Path path) throws DataStorageException
    {
        if (path == null)
        {
            throw new DataStorageException("Шлях до CSV не може бути null");
        }
        try
        {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        catch (IOException | SecurityException exception)
        {
            throw new DataStorageException("Не вдалося прочитати CSV: " + path, exception);
        }
    }
}
