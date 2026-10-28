package ua.lpnu.kzp.lab04;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/** Виводить довідку та версію застосунку без читання метеоданих. */
public final class ApplicationInfo
{
    private ApplicationInfo()
    {
    }

    /**
     * Читає ресурс {@code build-info.properties} і виводить версію та номер збірки.
     *
     * @throws IllegalStateException якщо ресурс відсутній або метадані неповні
     * @throws UncheckedIOException якщо сталася помилка читання ресурсу
     */
    public static void printVersion()
    {
        Properties buildInfo = new Properties();
        try (InputStream input = ApplicationInfo.class.getResourceAsStream("/build-info.properties"))
        {
            if (input == null)
            {
                throw new IllegalStateException("Build metadata is missing; build with Maven Wrapper");
            }
            buildInfo.load(input);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Cannot read build metadata", e);
        }

        String version = buildInfo.getProperty("app.version");
        String buildNumber = buildInfo.getProperty("build.number");
        if (version == null || version.isBlank() || version.contains("${") || buildNumber == null || buildNumber.isBlank() || buildNumber.contains("${"))
        {
            throw new IllegalStateException("Build metadata is incomplete");
        }
        System.out.printf("lab04 %s (build %s)%n", version, buildNumber);
    }

    /**
     * Виводить призначення програми, приклад запуску та доступні параметри.
     */
    public static void printHelp()
    {

        System.out.println("Метеостанція: аналіз вимірювань із CSV");
        System.out.println("Запуск: java -jar LAB_04/target/lab04.jar [параметри]");
        System.out.println("--help          показати цю довідку\n"
                           +
                           "--version       показати версію програми\n"
                           +
                           "--input ШЛЯХ    вказати вхідний CSV-файл\n"
                           +
                           "--output ШЛЯХ   вказати файл для звіту\n"
                           +
                           "--danger        показати додатковий аналіз небезпеки\n");
    }
}
