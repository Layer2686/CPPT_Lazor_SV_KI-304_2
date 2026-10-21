package ua.lpnu.kzp.lab02;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;
import java.util.Locale;

/**
 * Консольна метеостанція для варіанта 18 лабораторної роботи № 2.
 * Читає CSV, перевіряє записи та формує звіт із чотирма показниками й помилками.
 */
public class Main {

    /**
     * Створює екземпляр класу Main.
     */
    public Main() {
    }

    /**
     * Розбирає параметри запуску та запускає обробку CSV або виводить довідку чи версію.
     *
     * @param args параметри {@code --input}, {@code --output}, {@code --help} або {@code --version}
     */
    public static void main(String[] args) {
        Path inputPath = Path.of("data/input.csv");
        Path outputPath = Path.of("out/report.txt");
        if (args.length == 1 && "--version".equals(args[0]))
        {
            printVersion();
            return;
        }

        if (args.length == 1 && "--help".equals(args[0]))
        {
            printHelp();
            return;
        }

        for (int argIndex = 0; argIndex < args.length; argIndex += 2)
        {
            String optionName = args[argIndex];
            if (!"--input".equals(optionName) && !"--output".equals(optionName))
            {
                System.err.println("Невідомий параметр: " + optionName);
                return;
            }
            if(argIndex + 1 >= args.length)
            {
            System.err.println("Після " + optionName + " бракує шляху");
            return;
            }
            String pathText = args[argIndex + 1];


             if (pathText.startsWith("--"))
            {
                System.err.println("Після " + optionName + " бракує шляху");
                return;
            }

            if ("--input".equals(optionName))
            {
                inputPath = Path.of(pathText);
            }
            else if ("--output".equals(optionName))
            {
                outputPath = Path.of(pathText);
            }
            else
            {
            System.err.println("Невідомий параметр: " + optionName);
            return;
            }
        }
        processInput(inputPath, outputPath);
    }

    /**
     * Перетворює рядки на об’єкти, зберігаючи номер і причину помилки кожного рядка.
     * До звіту передає лише коректні вимірювання.
     *
     * @param inputPath шлях до вхідного CSV
     * @param outputPath шлях до звіту
     */
    private static void processInput(Path inputPath, Path outputPath) {
        try {
            List<String> lines = readInput(inputPath);
            List<WeatherReading> readings = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                try {
                    WeatherReading reading = WeatherReading.fromCsv(line);
                    readings.add(reading);
                    // Збережено консольне представлення записів із LAB_01.
                    System.out.println("Рядок " + (i + 1) + ": дата " + reading.getDate()
                            + ", температура " + reading.getTemperature() + " °C, вологість "
                            + reading.getHumidity() + " %, тиск " + reading.getPressure()
                            + " гПа, вітер " + reading.getWind() + " м/с");
                } catch (IllegalArgumentException e) {
                    // У LAB_01 повідомлення про кількість полів не містило вхідного рядка.
                    if ("очікується 5 полів".equals(e.getMessage())) {
                        errors.add(String.format(Locale.ROOT,
                                "Рядок %d: %s", i + 1, e.getMessage()));
                    } else {
                        errors.add(String.format(Locale.ROOT,
                                "Рядок %d: %s; вхід: %s", i + 1, e.getMessage(), line));
                    }
                }
            }
            String reportText = WeatherReport.format(readings, errors);
            System.out.print(reportText);
            writeReport(outputPath, reportText);
        } catch (IOException e) {
            System.err.println("Не вдалося прочитати файл " + inputPath + ": " + e.getMessage());
        }
    }

    /**
     * Створює потрібні папки й записує звіт у UTF-8, замінюючи попередній вміст файла.
     * Для шляху без папки записує файл у робочу директорію.
     * Помилки запису повідомляє через {@code System.err}.
     *
     * @param outputpath шлях до файла звіту
     * @param reportText готовий текст звіту
     */
    private static void writeReport(Path outputpath, String reportText)
    {
        try
        {
            Path directory = outputpath.getParent();
            if (directory != null)
            {
            Files.createDirectories(directory);
            }
            Files.writeString(outputpath,reportText, StandardCharsets.UTF_8);

        }
        catch(IOException e)
        {
            System.err.println("Неможливо записати звіт " + outputpath + ": " + e.getMessage());
        }

    }

    /**
     * Читає ресурс {@code build-info.properties} і виводить версію та номер збірки.
     *
     * @throws IllegalStateException якщо ресурс відсутній або метадані неповні
     * @throws UncheckedIOException якщо сталася помилка читання ресурсу
     */
    private static void printVersion()
    {
        Properties buildInfo = new Properties();
        try (InputStream input = Main.class.getResourceAsStream("/build-info.properties"))
        {
            if (input == null)
            {
                throw new IllegalStateException("Build metadata is missing; build with Maven Wrapper");
            }
            buildInfo.load(input);
        } catch (IOException e)
        {
            throw new UncheckedIOException("Cannot read build metadata", e);
        }

        String version = buildInfo.getProperty("app.version");
        String buildNumber = buildInfo.getProperty("build.number");
        if (version == null || version.isBlank() || version.contains("${")
            || buildNumber == null || buildNumber.isBlank() || buildNumber.contains("${"))
        {
            throw new IllegalStateException("Build metadata is incomplete");
        }
        System.out.printf("lab02 %s (build %s)%n", version, buildNumber);
    }

    /**
     * Виводить призначення програми, приклад запуску та доступні параметри.
     */
    private static void printHelp()
    {

        System.out.println("Метеостанція: аналіз вимірювань із CSV");
        System.out.println("Запуск: java -jar LAB_02/target/lab02.jar [параметри]");
        System.out.println("--help          показати цю довідку\n" +
            "--version       показати версію програми\n" +
            "--input ШЛЯХ    вказати вхідний CSV-файл\n" +
            "--output ШЛЯХ   вказати файл для звіту\n");


    }

    /**
     * Читає всі рядки вхідного файла у кодуванні UTF-8.
     *
     * @param inputPath шлях до вхідного файла
     * @return список прочитаних рядків
     * @throws IOException якщо файл неможливо прочитати або декодувати як UTF-8
     */
    private static List<String> readInput(Path inputPath) throws IOException
    {
        return Files.readAllLines(inputPath, StandardCharsets.UTF_8);
    }
}
