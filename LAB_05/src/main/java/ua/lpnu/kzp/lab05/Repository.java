package ua.lpnu.kzp.lab05;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Типобезпечне сховище з незалежними незмінними знімками списку.
 *
 * @param <T> тип записів сховища
 */
public final class Repository<T>
{
    private final List<T> items = new ArrayList<>();

    /** Створює порожнє сховище. */
    public Repository()
    {
    }

    /**
     * Додає запис, зберігаючи порядок і повторення.
     * @param item запис для додавання
     * @throws NullPointerException якщо item дорівнює null
     */
    public void add (T item)
    {
        items.add(requireNonNull(item));
    }
    /**
     * Повертає знімок, структура якого не змінюється після наступних додавань.
     * Самі об’єкти не клонуються.
     * @return незмінний список поточних записів
     */
    public List<T> all()
    {
        List<T> snapshot = List.copyOf(items);
        return snapshot;
    }
    /**
     * Відбирає записи за умовою без зміни сховища.
     * @param condition умова для T або його супертипу
     * @return незмінний список збігів у початковому порядку
     * @throws NullPointerException якщо condition дорівнює null
     */
    public List<T> find(Predicate<? super T> condition)
    {
        Objects.requireNonNull(condition);
        List<T> result = items.stream().filter(condition).toList();
        return result;
    }
}
