package lab05;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * JSON-масив на тих самих анотованих полях і порядку, що й CSV.
 * @param <T> тип плоских записів
 */
public final class JsonExporter<T> implements Exporter<T>
{
    /** Створює JSON-експортер. */
    public JsonExporter()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void write(Path path, Class<T> recordType, List<? extends T> records) throws DataStorageException
    {
        StorageFiles.write(path, toJson(recordType, records), "JSON");
    }

    /**
     * Формує JSON: числа й boolean лишаються своїми типами; null є JSON null.
     * Текст, символ, enum і календарне значення стають рядками.
     * Вкладені об’єкти та нескінченні числа відхиляються.
     * @param recordType клас записів
     * @param records записи
     * @return JSON-масив із завершальним LF
     * @throws DataStorageException якщо метадані або значення некоректні
     */
    public String toJson(Class<T> recordType, List<? extends T> records) throws DataStorageException
    {
        List<Field> columns = ColumnMetadata.columns(recordType, true);
        if (records == null)
        {
            throw new DataStorageException("Список записів не може бути null");
        }
        List<String> objects = new ArrayList<>();
        for (T record : records)
        {
            if (record == null || !recordType.isInstance(record))
            {
                throw new DataStorageException("Некоректний запис для класу " + recordType.getName());
            }
            List<String> properties = new ArrayList<>();
            for (Field field : columns)
            {
                try
                {
                    properties.add(quote(field.getAnnotation(CsvColumn.class).value()) + ": " + encode(field.get(record)));
                }
                catch (IllegalAccessException | IllegalArgumentException exception)
                {
                    throw new DataStorageException("Не вдалося прочитати поле: " + field.getName(), exception);
                }
            }
            objects.add("  {" + String.join(", ", properties) + "}");
        }
        return objects.isEmpty() ? "[]\n" : "[\n" + String.join(",\n", objects) + "\n]\n";
    }

    private static String encode(Object value) throws DataStorageException
    {
        if (value == null)
        {
            return "null";
        }
        if ((value instanceof Double || value instanceof Float)
                && !Double.isFinite(((Number) value).doubleValue()))
        {
            throw new DataStorageException("JSON не підтримує NaN або нескінченність");
        }
        if (value instanceof Boolean || value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long || value instanceof Float
                || value instanceof Double || value instanceof BigInteger || value instanceof BigDecimal)
        {
            return value.toString();
        }
        if (value instanceof String || value instanceof Character || value instanceof Enum<?>
                || value instanceof TemporalAccessor)
        {
            return quote(value.toString());
        }
        throw new DataStorageException("Непідтримуваний плоский JSON-тип: " + value.getClass().getName());
    }

    private static String quote(String value)
    {
        StringBuilder result = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++)
        {
            char symbol = value.charAt(index);
            switch (symbol)
            {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default ->
                {
                    if (symbol < 0x20 || Character.isSurrogate(symbol))
                    {
                        result.append(String.format(Locale.ROOT, "\\u%04x", (int) symbol));
                    }
                    else
                    {
                        result.append(symbol);
                    }
                }
            }
        }
        return result.append('"').toString();
    }
}
