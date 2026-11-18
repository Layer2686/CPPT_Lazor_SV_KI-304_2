package lab05;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Читає новий CSV із заголовком і відновлює типізовані метеозаписи. */
public final class WeatherCsvStorage
{
    /** Створює сервіс імпорту. */
    public WeatherCsvStorage()
    {
    }

    /**
     * Читає весь UTF-8 документ. Порожній файл і лише заголовок дають порожній список.
     * Заголовок має точно відповідати погодженому порядку; BOM не допускається.
     * @param path файл нового CSV
     * @return незмінний список зі збереженням порядку й дублікатів
     * @throws DataStorageException якщо файл, структура або значення некоректні
     */
    public List<WeatherReadingRecord> read(Path path) throws DataStorageException
    {
        List<List<String>> rows = CsvParser.parseDocument(StorageFiles.read(path));
        if (rows.isEmpty())
        {
            return List.of();
        }
        if (!rows.get(0).equals(WeatherReadingRecord.csvHeader()))
        {
            throw new DataStorageException("Некоректний CSV-заголовок; очікується: "
                    + String.join(",", WeatherReadingRecord.csvHeader()));
        }
        List<WeatherReadingRecord> records = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++)
        {
            try
            {
                records.add(WeatherReadingRecord.fromFields(rows.get(index)));
            }
            catch (DataStorageException exception)
            {
                Throwable cause = exception.getCause();
                throw new DataStorageException("Запис " + (index + 1) + ": " + exception.getMessage(),
                        cause == null ? exception : cause);
            }
        }
        return List.copyOf(records);
    }
}
