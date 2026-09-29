import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
