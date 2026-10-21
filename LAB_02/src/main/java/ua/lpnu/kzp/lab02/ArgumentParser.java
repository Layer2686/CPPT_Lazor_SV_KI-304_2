package ua.lpnu.kzp.lab02;

import java.nio.file.Path;

/** Розбирає аргументи; не читає файли та не запускає обробку даних. */
public final class ArgumentParser {
    private ArgumentParser() {
    }

    /**
     * Перевіряє ключі та їхні значення, зберігаючи правила попередньої реалізації.
     * Якщо ключ шляху повторений, використовується останнє значення.
     *
     * @param args параметри командного рядка
     * @return перевірені параметри запуску
     * @throws IllegalArgumentException якщо параметр невідомий або шлях не задано
     */
    public static CommandLineOptions parse(String[] args) {
        Path inputPath = Path.of("data/input.csv");
        Path outputPath = Path.of("out/report.txt");
        if (args.length == 1 && "--help".equals(args[0])) {
            return new CommandLineOptions(inputPath, outputPath, true, false);
        }
        if (args.length == 1 && "--version".equals(args[0])) {
            return new CommandLineOptions(inputPath, outputPath, false, true);
        }
        for (int argIndex = 0; argIndex < args.length; argIndex += 2) {
            String optionName = args[argIndex];
            if (!"--input".equals(optionName) && !"--output".equals(optionName)) {
                throw new IllegalArgumentException("Невідомий параметр: " + optionName);
            }
            if (argIndex + 1 >= args.length || args[argIndex + 1].startsWith("--")) {
                throw new IllegalArgumentException("Після " + optionName + " бракує шляху");
            }
            Path path = Path.of(args[argIndex + 1]);
            if ("--input".equals(optionName)) {
                inputPath = path;
            } else {
                outputPath = path;
            }
        }
        return new CommandLineOptions(inputPath, outputPath, false, false);
    }
}
