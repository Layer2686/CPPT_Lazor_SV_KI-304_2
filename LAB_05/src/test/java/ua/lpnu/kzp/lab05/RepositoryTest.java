package ua.lpnu.kzp.lab05;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Перевірки незалежного знімка, типобезпечного пошуку та помилкових аргументів. */
class RepositoryTest
{
    @Test
    void snapshotPreservesDuplicatesAndDoesNotChangeAfterAdd()
    {
        Repository<String> repository = new Repository<>();
        repository.add("А");
        repository.add("А");
        List<String> snapshot = repository.all();
        repository.add("Б");

        assertEquals(List.of("А", "А"), snapshot);
        assertEquals(List.of("А", "А", "Б"), repository.all());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("В"));
    }

    @Test
    void findAcceptsSupertypePredicateAndPreservesOrder()
    {
        Repository<String> repository = new Repository<>();
        repository.add("Київ");
        repository.add("Львів");
        repository.add("Київ");
        Predicate<CharSequence> fourLetters = text -> text.length() == 4;
        List<String> found = repository.find(fourLetters);

        assertEquals(List.of("Київ", "Київ"), found);
        assertEquals(List.of(), repository.find(text -> text.isEmpty()));
        assertThrows(UnsupportedOperationException.class, () -> found.clear());
        assertEquals(List.of("Київ", "Львів", "Київ"), repository.all());
    }

    @Test
    void anotherElementTypeAndEmptyRepositoryWork()
    {
        Repository<Integer> repository = new Repository<>();
        assertEquals(List.of(), repository.all());
        assertEquals(List.of(), repository.find(number -> number > 0));
        repository.add(-2);
        repository.add(3);

        assertEquals(List.of(3), repository.find(number -> number > 0));
    }

    @Test
    void nullItemAndPredicateAreRejectedWithoutChangingStorage()
    {
        Repository<String> repository = new Repository<>();
        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
        assertEquals(List.of(), repository.all());
    }
}
