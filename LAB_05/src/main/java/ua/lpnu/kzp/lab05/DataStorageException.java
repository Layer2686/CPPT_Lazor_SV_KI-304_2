package ua.lpnu.kzp.lab05;

import java.io.Serial;

/** Checked-виняток операцій зберігання, формату даних і рефлексивного експорту. */
public class DataStorageException extends Exception
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Створює помилку з поясненням, коли окремої технічної причини немає.
     * @param message пояснення невдалої операції
     */
    public DataStorageException(String message)
    {
        super(message);
    }

    /**
     * Створює помилку з контекстом операції та початковою причиною.
     * @param message пояснення невдалої операції
     * @param cause початковий виняток
     */
    public DataStorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
