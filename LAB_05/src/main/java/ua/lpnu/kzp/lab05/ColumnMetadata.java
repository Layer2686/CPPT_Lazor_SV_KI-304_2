package ua.lpnu.kzp.lab05;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Спільні детерміновані метадані для CSV/JSON; кеш лише для класів, не об’єктів. */
final class ColumnMetadata
{
    private static final ConcurrentMap<Class<?>, List<Field>> CACHE = new ConcurrentHashMap<>();

    private ColumnMetadata()
    {
    }

    static List<Field> columns(Class<?> recordType, boolean cached) throws DataStorageException
    {
        if (recordType == null)
        {
            throw new DataStorageException("Клас записів не може бути null");
        }
        if (!cached)
        {
            return inspect(recordType);
        }
        List<Field> existing = CACHE.get(recordType);
        if (existing != null)
        {
            return existing;
        }
        List<Field> inspected = inspect(recordType);
        List<Field> previous = CACHE.putIfAbsent(recordType, inspected);
        return previous == null ? inspected : previous;
    }

    private static List<Field> inspect(Class<?> recordType) throws DataStorageException
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
        Set<String> names = new HashSet<>();
        for (Field field : columns)
        {
            String name = field.getAnnotation(CsvColumn.class).value();
            if (name.isBlank() || !names.add(name))
            {
                throw new DataStorageException("Порожня або повторена назва колонки: " + name);
            }
        }
        return columns;
    }
}
