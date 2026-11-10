package ua.lpnu.kzp.lab05;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Перевіряє повідомлення та збереження початкової технічної причини. */
class DataStorageExceptionTest
{
    @Test
    void messageOnlyConstructorPreservesExplanationWithoutCause()
    {
        DataStorageException error = new DataStorageException("Очікується 5 CSV-полів");

        assertEquals("Очікується 5 CSV-полів", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void messageAndCauseConstructorPreservesOriginalException()
    {
        List<Throwable> causes = List.of(
                new IOException("Немає доступу"),
                new NumberFormatException("Хибне число"),
                new DateTimeParseException("Хибна дата", "2026-02-31", 0),
                new IllegalAccessException("Немає доступу до поля"));

        for (Throwable cause : causes)
        {
            DataStorageException error = new DataStorageException("Не вдалося обробити дані", cause);
            assertEquals("Не вдалося обробити дані", error.getMessage());
            assertSame(cause, error.getCause());
        }
    }
}
