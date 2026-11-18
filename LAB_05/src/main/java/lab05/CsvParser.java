package lab05;

import java.util.ArrayList;
import java.util.List;

/** Розбирає цілий CSV-документ, включно з переносами всередині лапок. */
public final class CsvParser
{
    private enum State
    {
        START, UNQUOTED, QUOTED, AFTER_QUOTE
    }

    private CsvParser()
    {
    }

    /**
     * Розбирає коми, подвоєні лапки та CR/LF/CRLF; значення не обрізаються.
     * Порожній документ дає порожній список, порожній рядок — одну порожню клітинку.
     * Один завершальний роздільник рядків не додає зайвого запису.
     * @param document повний текст без BOM
     * @return незмінні записи з незмінними списками клітинок
     * @throws DataStorageException якщо текст null або синтаксис лапок неправильний
     */
    public static List<List<String>> parseDocument(String document) throws DataStorageException
    {
        if (document == null)
        {
            throw new DataStorageException("CSV-документ не може бути null");
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        State state = State.START;
        boolean pendingRow = false;
        for (int index = 0; index < document.length(); index++)
        {
            char symbol = document.charAt(index);
            pendingRow = true;
            if (state == State.QUOTED)
            {
                if (symbol == '"')
                {
                    state = State.AFTER_QUOTE;
                }
                else
                {
                    value.append(symbol);
                }
                continue;
            }
            if (state == State.AFTER_QUOTE && symbol == '"')
            {
                value.append('"');
                state = State.QUOTED;
                continue;
            }
            if (symbol == ',' || symbol == '\r' || symbol == '\n')
            {
                row.add(value.toString());
                value.setLength(0);
                state = State.START;
                if (symbol != ',')
                {
                    rows.add(List.copyOf(row));
                    row.clear();
                    pendingRow = false;
                    if (symbol == '\r' && index + 1 < document.length() && document.charAt(index + 1) == '\n')
                    {
                        index++;
                    }
                }
                continue;
            }
            if (state == State.AFTER_QUOTE)
            {
                throw new DataStorageException("Запис " + (rows.size() + 1) + ": символ після закривальної лапки");
            }
            if (symbol == '"')
            {
                if (state != State.START)
                {
                    throw new DataStorageException("Запис " + (rows.size() + 1) + ": лапка всередині неекранованого поля");
                }
                state = State.QUOTED;
            }
            else
            {
                value.append(symbol);
                state = State.UNQUOTED;
            }
        }
        if (state == State.QUOTED)
        {
            throw new DataStorageException("Запис " + (rows.size() + 1) + ": незакриті лапки");
        }
        if (pendingRow)
        {
            row.add(value.toString());
            rows.add(List.copyOf(row));
        }
        return List.copyOf(rows);
    }
}
