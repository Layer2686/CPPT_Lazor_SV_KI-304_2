package ua.lpnu.kzp.lab05;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.lang.reflect.InaccessibleObjectException;
/**
 * Універсальний CSV-експорт власних анотованих полів плоского класу.
 * @param <T> тип записів
 */
public final class CsvExporter<T>
{
    /** Створює експортер. */
    public CsvExporter()
    {
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
        if (recordType == null)
        {
            throw new DataStorageException("Клас записів не може бути null");
        }
        Field[] fields;
        try
        {
            fields = recordType.getDeclaredFields();
        }
        catch (SecurityException exception)
        {
            throw new DataStorageException("Немає доступу до класу: " + recordType.getName(), exception);
        }
        List<Field> columns = Arrays.stream(fields).filter(field -> field.isAnnotationPresent(CsvColumn.class))
            .filter(field -> !Modifier.isStatic(field.getModifiers())).sorted(Comparator.comparing(Field::getName)).toList();
        if (columns.isEmpty())
        {
            throw new DataStorageException("Клас не містить полів для CSV");
        }
        for (Field field : columns)
        {
            try
            {
                if (!field.trySetAccessible())
                {
                    throw new DataStorageException("Немає доступу до поля: " + field.getName());
                }
            }
            catch (SecurityException | InaccessibleObjectException exception)
            {
                throw new DataStorageException("Немає доступу до поля: " + field.getName(), exception);
            }
        }
        return columns;
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
