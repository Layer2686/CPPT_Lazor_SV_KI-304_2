package ua.lpnu.kzp.lab05;

import java.nio.file.Path;

/** Перевіряє параметри без файлових операцій та обробки вимірювань. */
public final class ArgumentParser
{
    private ArgumentParser()
    {
    }

    /**
     * Розбирає шляхи та прапорці; повторений параметр шляху використовує останнє значення.
     * Старий --input та новий --import-csv є взаємовиключними джерелами.
     * @param args аргументи командного рядка
     * @return перевірені параметри
     * @throws IllegalArgumentException якщо ключі, значення або комбінації некоректні
     */
    public static CommandLineOptions parse(String[] args)
    {
        Path input = Path.of("data/input.csv");
        Path output = Path.of("out/report.txt");
        Path importCsv = null;
        Path exportCsv = null;
        Path exportJson = null;
        boolean explicitInput = false;
        boolean danger = false;
        boolean queries = false;
        boolean roundTrip = false;
        if (args.length == 1 && "--help".equals(args[0]))
        {
            return new CommandLineOptions(input, output, true, false, false, false);
        }
        if (args.length == 1 && "--version".equals(args[0]))
        {
            return new CommandLineOptions(input, output, false, true, false, false);
        }
        for (int index = 0; index < args.length; index++)
        {
            String option = args[index];
            switch (option)
            {
                case "--danger" -> danger = true;
                case "--queries" -> queries = true;
                case "--round-trip" -> roundTrip = true;
                case "--input", "--output", "--import-csv", "--export-csv", "--export-json" ->
                {
                    if (index + 1 >= args.length || args[index + 1].startsWith("--"))
                    {
                        throw new IllegalArgumentException("Після " + option + " бракує шляху");
                    }
                    Path path = Path.of(args[++index]);
                    switch (option)
                    {
                        case "--input" ->
                        {
                            input = path;
                            explicitInput = true;
                        }
                        case "--output" -> output = path;
                        case "--import-csv" -> importCsv = path;
                        case "--export-csv" -> exportCsv = path;
                        case "--export-json" -> exportJson = path;
                        default -> throw new IllegalStateException("Непередбачений параметр шляху");
                    }
                }
                default -> throw new IllegalArgumentException("Невідомий параметр: " + option);
            }
        }
        if (explicitInput && importCsv != null)
        {
            throw new IllegalArgumentException("Виберіть лише --input або --import-csv");
        }
        return new CommandLineOptions(input, output, false, false, danger, queries,
                importCsv, exportCsv, exportJson, roundTrip);
    }
}
