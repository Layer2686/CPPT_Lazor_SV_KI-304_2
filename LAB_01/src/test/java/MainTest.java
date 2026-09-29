import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void printsGreetingExactly67Times() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            try {
                System.setOut(capturedOut);
                Main.main(new String[0]);
            } finally {
                System.setOut(originalOut);
            }
        }

        List<String> lines = output.toString(StandardCharsets.UTF_8).lines().toList();
        assertEquals(67, lines.size());
        for (String line : lines) {
            assertEquals("Hello, world!", line);
        }
    }

    @Test
    void versionFlagPrintsFilteredBuildMetadata() throws IOException {
        Properties buildInfo = new Properties();
        try (InputStream input = Main.class.getResourceAsStream("/build-info.properties")) {
            assertNotNull(input);
            buildInfo.load(input);
        }

        assertEquals("1.0.0", buildInfo.getProperty("app.version"));
        String buildNumber = buildInfo.getProperty("build.number");
        assertNotNull(buildNumber);
        assertTrue(buildNumber.matches("local|[1-9][0-9]*"));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            try {
                System.setOut(capturedOut);
                Main.main(new String[] {"--version"});
            } finally {
                System.setOut(originalOut);
            }
        }

        assertEquals("lab01 1.0.0 (build " + buildNumber + ")" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }
}
