package lab05;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.nio.file.Path;
/**
 * Універсальний CSV-експорт власних анотованих полів плоского класу.
 * @param <T> тип записів
 */
public final class CsvExporter<T> implements Exporter<T>
{
    private final boolean cacheEnabled;

    /** Створює експортер із кешуванням метаданих. */
    public CsvExporter()
    {
        this(true);
    }

    /**
     * Дозволяє порівняти експорт із кешем і без нього.
     * @param cacheEnabled чи використовувати кеш полів за класом
     */
    public CsvExporter(boolean cacheEnabled)
    {
        this.cacheEnabled = cacheEnabled;
    }

    /**
     * Записує CSV у UTF-8, створюючи папки й замінюючи старий файл.
     * Null-поле стає порожньою клітинкою; null-запис заборонений.
     * Static та неанотовані поля пропускаються; успадковані поля не обходяться.
     * @param path шлях призначення
     * @param recordType клас записів, потрібний і для порожнього списку
     * @param records записи у порядку експорту
     * @throws DataStorageException якщо дані, доступ або запис некоректні
     */
    @Override
    public void write(Path path, Class<T> recordType, List<? extends T> records) throws DataStorageException
    {
        String text = toCsv(recordType, records);
        StorageFiles.write(path, text, "CSV");
    }

    /**
     * Формує CSV без файлових операцій.
     * @param recordType клас записів
     * @param records записи
     * @return CSV із заголовком та завершальним LF
     * @throws DataStorageException якщо запис або метадані некоректні
     */
    public String toCsv(Class<T> recordType, List<? extends T> records) throws DataStorageException
    {
        if (records == null)
        {
            throw new DataStorageException("Список записів не може бути null");
        }
        List<Field> columns = getColumns(recordType);
        StringBuilder csv = new StringBuilder(buildHeader(columns)).append('\n');
        for (T record : records)
        {
            if (record == null || !recordType.isInstance(record))
            {
                throw new DataStorageException("Некоректний запис для класу " + recordType.getName());
            }
            List<String> values = new ArrayList<>();
            for (Field field : columns)
            {
                try
                {
                    values.add(escapeCsv(Objects.toString(field.get(record), "")));
                }
                catch (IllegalAccessException | IllegalArgumentException exception)
                {
                    throw new DataStorageException("Не вдалося прочитати поле: " + field.getName(), exception);
                }
            }
            csv.append(String.join(",", values)).append('\n');
        }
        return csv.toString();
    }

    private String buildHeader(List<Field> columns)
    {
        return columns.stream()
                .map(field -> field.getAnnotation(CsvColumn.class).value())
                .map(name -> escapeCsv(name))
                .collect(Collectors.joining(","));
    }

    private List<Field> getColumns(Class<T> recordType) throws DataStorageException
    {
        return ColumnMetadata.columns(recordType, cacheEnabled);
    }

    private String escapeCsv(String value)
    {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r"))
        {
            String changed = value.replace("\"", "\"\"");
            String wrapped = "\"" + changed + "\"";
            return wrapped;
        }
        return value;
    }
}
