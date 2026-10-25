package ua.lpnu.kzp.lab03;

import java.nio.file.Path;

/**
 * Параметри запуску після перевірки командного рядка.
 *
 * @param inputPath шлях до CSV
 * @param outputPath шлях до звіту
 * @param help чи потрібно показати лише довідку
 * @param version чи потрібно показати лише версію
 * @param danger чи потрібно показати додатковий аналіз небезпеки
 */
public record CommandLineOptions(Path inputPath, Path outputPath, boolean help, boolean version, boolean danger)
{
    /**
     * Перевіряє наявність шляхів і сумісність режимів.
     *
     * @throws IllegalArgumentException якщо шляху немає або вибрано обидва режими
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
    }
}
