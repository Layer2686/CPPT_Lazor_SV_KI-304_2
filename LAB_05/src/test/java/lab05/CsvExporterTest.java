package lab05;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest
{
    @TempDir
    Path directory;

    private static final class Note
    {
        @CsvColumn("Текст") private final String text;
        @CsvColumn("Код") private final int code;
        @CsvColumn("Не експортувати") private static final String SHARED = "static";
        private final String hidden = "неанотоване";

        Note(int code, String text)
        {
            this.code = code;
            this.text = text;
        }
    }

    @Test
    void exportsAnotherTypeWithQuotesUnicodeAndMultilineWithoutMutation() throws Exception
    {
        Note note = new Note(7, "Львів, \"Захід\"\r\nДругий рядок");
        List<Note> notes = List.of(note);
        Path path = directory.resolve("nested/notes.csv");
        new CsvExporter<Note>().write(path, Note.class, notes);
        assertEquals("Код,Текст\n7,\"Львів, \"\"Захід\"\"\r\nДругий рядок\"\n",
                Files.readString(path, StandardCharsets.UTF_8));
        assertEquals("Львів, \"Захід\"\r\nДругий рядок", note.text);
        assertEquals("неанотоване", note.hidden);
        assertSame(note, notes.get(0));
    }

    @Test
    void emptyExportHasHeaderAndShorterExportTruncatesFile() throws Exception
    {
        Path path = directory.resolve("notes.csv");
        CsvExporter<Note> exporter = new CsvExporter<>();
        exporter.write(path, Note.class, List.of(new Note(1, "довгий текст")));
        exporter.write(path, Note.class, List.of());
        assertEquals("Код,Текст\n", Files.readString(path));
        assertEquals("Код,Текст\n1,\n", exporter.toCsv(Note.class, List.of(new Note(1, null))));
    }

    @Test
    void rejectsInvalidMetadataAndNullRecordsBeforeChangingFile() throws Exception
    {
        assertThrows(DataStorageException.class, () -> new CsvExporter<String>().toCsv(String.class, List.of()));
        assertThrows(DataStorageException.class, () -> new CsvExporter<Note>().toCsv(null, List.of()));
        assertThrows(DataStorageException.class, () -> new CsvExporter<Note>().toCsv(Note.class, null));
        Path path = directory.resolve("existing.csv");
        Files.writeString(path, "original");
        assertThrows(DataStorageException.class,
                () -> new CsvExporter<Note>().write(path, Note.class, Arrays.asList((Note) null)));
        assertEquals("original", Files.readString(path));
    }

    @Test
    void fileErrorsKeepIOExceptionCause() throws Exception
    {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> new CsvExporter<Note>().write(directory, Note.class, List.of()));
        assertInstanceOf(IOException.class, exception.getCause());
        assertTrue(exception.getMessage().contains(directory.toString()));
        Path blocker = directory.resolve("file");
        Files.writeString(blocker, "x");
        assertInstanceOf(IOException.class, assertThrows(DataStorageException.class,
                () -> new CsvExporter<Note>().write(blocker.resolve("child.csv"), Note.class, List.of())).getCause());
    }
}
