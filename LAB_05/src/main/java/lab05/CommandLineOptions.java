package lab05;

import java.nio.file.Path;

/**
 * Незмінні перевірені параметри запуску.
 * @param inputPath старий CSV без заголовка з роздільником ;
 * @param outputPath стандартний текстовий звіт
 * @param help показати довідку
 * @param version показати версію
 * @param danger додатковий аналіз небезпеки
 * @param queries потокові запити
 * @param importCsvPath новий CSV із заголовком або null
 * @param exportCsvPath призначення CSV або null
 * @param exportJsonPath призначення JSON або null
 * @param roundTrip порівняти початкові й повторно прочитані CSV-записи
 */
public record CommandLineOptions(Path inputPath, Path outputPath, boolean help, boolean version,
        boolean danger, boolean queries, Path importCsvPath, Path exportCsvPath,
        Path exportJsonPath, boolean roundTrip)
{
    /**
     * Перевіряє шляхи та сумісність режимів.
     * @throws IllegalArgumentException якщо обов’язковий шлях відсутній або режими несумісні
     */
    public CommandLineOptions
    {
        if (inputPath == null || outputPath == null)
        {
            throw new IllegalArgumentException("Шляхи не можуть бути null");
        }
        if (help && version)
        {
            throw new IllegalArgumentException("Довідка та версія — окремі режими запуску");
        }
        if (roundTrip && exportCsvPath == null)
        {
            throw new IllegalArgumentException("Для --round-trip потрібен --export-csv ШЛЯХ");
        }
    }

    /**
     * Зберігає шестипараметровий спосіб створення попередніх режимів.
     * @param inputPath старий вхід
     * @param outputPath текстовий звіт
     * @param help довідка
     * @param version версія
     * @param danger аналіз небезпеки
     * @param queries потокові запити
     */
    public CommandLineOptions(Path inputPath, Path outputPath, boolean help, boolean version,
            boolean danger, boolean queries)
    {
        this(inputPath, outputPath, help, version, danger, queries, null, null, null, false);
    }
}
