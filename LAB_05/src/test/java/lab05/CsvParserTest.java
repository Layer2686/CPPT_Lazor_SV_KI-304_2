package lab05;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest
{
    @Test
    void readsQuotesCommasAndExactMultilineContent() throws Exception
    {
        String csv = "Код,Текст\r\n7,\"Львів, \"\"Захід\"\"\r\nДругий рядок\"\r\n";
        List<List<String>> rows = CsvParser.parseDocument(csv);
        assertEquals(List.of(List.of("Код", "Текст"), List.of("7", "Львів, \"Захід\"\r\nДругий рядок")), rows);
        assertEquals(3, csv.lines().count());
        assertEquals(2, rows.size());
        assertThrows(UnsupportedOperationException.class, () -> rows.clear());
        assertThrows(UnsupportedOperationException.class, () -> rows.get(0).clear());
    }

    @Test
    void handlesAllRecordSeparatorsAndTrailingEmptyFields() throws Exception
    {
        for (String separator : List.of("\n", "\r\n", "\r"))
        {
            assertEquals(List.of(List.of("a", ""), List.of("", "b")),
                    CsvParser.parseDocument("a," + separator + ",b" + separator));
        }
        assertEquals(List.of(List.of("", "", "")), CsvParser.parseDocument(",,"));
        assertEquals(List.of(List.of("")), CsvParser.parseDocument("\"\""));
        assertEquals(List.of(List.of("")), CsvParser.parseDocument("\n"));
        assertEquals(List.of(List.of("a"), List.of("")), CsvParser.parseDocument("a\n\n"));
        assertEquals(List.of(), CsvParser.parseDocument(""));
        assertEquals(List.of(List.of("a", "b")), CsvParser.parseDocument("a,b"));
    }

    @Test
    void rejectsMalformedQuotesAndNull()
    {
        for (String bad : List.of("\"not closed", "a\"b", "\"a\"x", "\"a\" ", "\"a\"\""))
        {
            DataStorageException exception = assertThrows(DataStorageException.class,
                    () -> CsvParser.parseDocument(bad), bad);
            assertTrue(exception.getMessage().contains("Запис 1"));
        }
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument(null));
    }
}
