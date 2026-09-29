import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && "--version".equals(args[0])) {
            printVersion();
            return;
        }

        int num = 67;
        String message = "Hello, world!";
        for (int i = 0; i < num; i++) {
            System.out.println(message);
        }
    }

    private static void printVersion() {
        Properties buildInfo = new Properties();
        try (InputStream input = Main.class.getResourceAsStream("/build-info.properties")) {
            if (input == null) {
                throw new IllegalStateException("Build metadata is missing; build with Maven Wrapper");
            }
            buildInfo.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read build metadata", e);
        }

        String version = buildInfo.getProperty("app.version");
        String buildNumber = buildInfo.getProperty("build.number");
        if (version == null || version.isBlank() || version.contains("${")
                || buildNumber == null || buildNumber.isBlank() || buildNumber.contains("${")) {
            throw new IllegalStateException("Build metadata is incomplete");
        }
        System.out.printf("lab01 %s (build %s)%n", version, buildNumber);
    }
}
