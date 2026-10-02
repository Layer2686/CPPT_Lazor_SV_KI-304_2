import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        Path inputPath = Path.of("data/input.csv");
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

        if (args.length == 2 && "--input".equals(args[0]))
        {
            inputPath = Path.of(args[1]);
            try
            {
                List<String> lines = readInput(inputPath);
                int validCount = 0;
                double temperatureMin = 0;
                double humiditySum = 0;
                double windSpeed_max = 0;
                for (int i = 0; i < lines.size(); i++)
                {
                    String line = lines.get(i);
                    String[] fields = line.split(";", -1);
                    if (fields.length != 5)
                    {
                        System.err.println("Рядок " + (i + 1) + ": очікується 5 полів");
                        continue;
                    }
                    double humidity;
                    double windSpeed;
                    double temperature;
                    double pressure;
                    String date = fields[0].trim();
                    String currentField = "вологість";
                    try {
                        humidity = Double.parseDouble(fields[2].trim());
                        if (humidity < 0 || humidity > 100 || !Double.isFinite(humidity)) {
                            System.err.println("Рядок " + (i + 1) + ": вологість має бути від 0 до 100; вхід: " + line);
                            continue;
                        }
                        currentField = "вітер";

                        windSpeed = Double.parseDouble(fields[4].trim());
                        if (windSpeed < 0 || !Double.isFinite(windSpeed)) {
                            System.err.println("Рядок " + (i + 1) + ": вітер має бути скінченним невід’ємним числом; вхід: " + line);
                            continue;
                        }

                        currentField = "температура";
                        temperature = Double.parseDouble(fields[1].trim());
                        if (!Double.isFinite(temperature)) {
                            System.err.println("Рядок " + (i + 1) + ": температура має бути скінченним числом; вхід: " + line);
                            continue;
                        }

                        currentField = "тиск";
                        pressure = Double.parseDouble(fields[3].trim());
                        if (pressure <= 0 || !Double.isFinite(pressure)) {
                            System.err.println("Рядок " + (i + 1) + ": тиск має бути додатним скінченним числом; вхід: " + line);
                            continue;
                        }

                    }
                    catch(NumberFormatException e)
                    {
                        System.err.println("Рядок " + (i + 1) + ": "
                            + currentField + " не є числом; вхід: " + line);
                        continue;
                    }
                    if (!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
                    {
                        System.err.println("Рядок " + (i + 1) + ": Дата задана неправильно; вхід: " + line);
                        continue;
                    }
                    System.out.println("Рядок " + (i + 1) + ": дата " + date + ", температура " + temperature + " °C, вологість " + humidity +
                        " %, тиск " + pressure + " гПа, вітер " + windSpeed + " м/с");
                    if (validCount == 0 || temperature < temperatureMin)
                    {
                        temperatureMin = temperature;
                    }
                    humiditySum += humidity;
                    if (validCount == 0 || windSpeed > windSpeed_max)
                    {
                        windSpeed_max = windSpeed;
                    }
                    validCount++;
                }
                System.out.println("Кількість коректних записів: " + validCount);
            }
            catch (IOException e)
            {
                System.err.println("Не вдалося прочитати файл " + inputPath + ": " + e.getMessage());
            }
            return;

        }

        int num = 67;
        String message = "Hello, world!";
        for (int i = 0; i < num; i++)
        {
            System.out.println(message);
        }
    }

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
        System.out.printf("lab01 %s (build %s)%n", version, buildNumber);
    }

    private static void printHelp()
    {

        System.out.println("Метеостанція: аналіз вимірювань із CSV");
        System.out.println("Запуск: java -jar LAB_01/target/lab01.jar [параметри]");
        System.out.println("--help          показати цю довідку\n" +
            "--version       показати версію програми\n" +
            "--input ШЛЯХ    вказати вхідний CSV-файл\n" +
            "--output ШЛЯХ   вказати файл для звіту\n");


    }

    private static List<String> readInput(Path inputPath) throws IOException
    {
        return Files.readAllLines(inputPath, StandardCharsets.UTF_8);
    }
}
