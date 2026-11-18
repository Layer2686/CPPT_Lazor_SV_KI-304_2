package ua.lpnu.kzp.lab05;

import java.nio.file.Path;
import java.util.List;

/**
 * Спільний контракт запису анотованих об’єктів у вибраному форматі.
 * @param <T> тип записів
 */
public interface Exporter<T>
{
    /**
     * Записує впорядкований набір у UTF-8 файл.
     * @param path шлях призначення
     * @param recordType клас, що задає метадані навіть для порожнього списку
     * @param records записи
     * @throws DataStorageException якщо метадані, значення або файл некоректні
     */
    void write(Path path, Class<T> recordType, List<? extends T> records) throws DataStorageException;
}
