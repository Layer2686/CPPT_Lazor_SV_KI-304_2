package ua.lpnu.kzp.lab03;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgumentParserTest
{
    @Test
    void usesDefaultPathsWithoutArguments()
    {
        CommandLineOptions options = ArgumentParser.parse(new String[0]);

        assertEquals(Path.of("data/input.csv"), options.inputPath());
        assertEquals(Path.of("out/report.txt"), options.outputPath());
        assertFalse(options.help());
        assertFalse(options.version());
        assertFalse(options.danger());
    }

    @Test
    void acceptsBothOrdersAndKeepsLastRepeatedPath()
    {
        CommandLineOptions direct = ArgumentParser.parse(
                new String[]
                {
                    "--input", "дані.csv", "--output", "звіт.txt"
                });
        CommandLineOptions reversed = ArgumentParser.parse(
                new String[]
                {
                    "--output", "звіт.txt", "--input", "дані.csv"
                });

        assertEquals(direct, reversed);
        assertEquals(Path.of("дані.csv"), direct.inputPath());
        assertEquals(Path.of("звіт.txt"), direct.outputPath());
        CommandLineOptions repeated = ArgumentParser.parse(
                new String[]
                {
                    "--input", "old.csv", "--input", "new.csv"
                });
        assertEquals(Path.of("new.csv"), repeated.inputPath());
    }

    @Test
    void recognizesHelpAndVersionOnlyAsSeparateModes()
    {
        CommandLineOptions help = ArgumentParser.parse(new String[]
        {
            "--help"
        });
        CommandLineOptions version = ArgumentParser.parse(new String[]
        {
            "--version"
        });

        assertTrue(help.help());
        assertFalse(help.version());
        assertTrue(version.version());
        assertFalse(version.help());
        assertFalse(help.danger());
        assertFalse(version.danger());
        assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--help", "--version"
                }));
    }

    @Test
    void unknownKeyIsReportedBeforeItsMissingValue()
    {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--unknown"
                }));

        assertEquals("Невідомий параметр: --unknown", error.getMessage());
    }

    @Test
    void rejectsMissingPathAndAnotherOptionInsteadOfPath()
    {
        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--input"
                }));
        IllegalArgumentException nextOption = assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--input", "--output"
                }));

        assertEquals("Після --input бракує шляху", missing.getMessage());
        assertEquals(missing.getMessage(), nextOption.getMessage());
    }

    @Test
    void optionsRejectMissingPathsAndConflictingModes()
    {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandLineOptions(null, Path.of("report.txt"), false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new CommandLineOptions(Path.of("input.csv"), null, false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new CommandLineOptions(Path.of("input.csv"), Path.of("report.txt"), true, true, false));
    }

    @Test
    void recognizesDangerWithoutConsumingAPath()
    {
        CommandLineOptions options = ArgumentParser.parse(new String[]
        {
            "--danger"
        });

        assertTrue(options.danger());
        assertEquals(Path.of("data/input.csv"), options.inputPath());
        assertEquals(Path.of("out/report.txt"), options.outputPath());
        assertFalse(options.help());
        assertFalse(options.version());
    }

    @Test
    void dangerWorksBeforeBetweenAndAfterPathOptions()
    {
        CommandLineOptions expected = new CommandLineOptions(
                Path.of("input.csv"), Path.of("report.txt"), false, false, true);

        assertEquals(expected, ArgumentParser.parse(new String[]
        {
            "--danger", "--input", "input.csv", "--output", "report.txt"
        }));
        assertEquals(expected, ArgumentParser.parse(new String[]
        {
            "--input", "input.csv", "--danger", "--output", "report.txt"
        }));
        assertEquals(expected, ArgumentParser.parse(new String[]
        {
            "--output", "report.txt", "--input", "input.csv", "--danger"
        }));
    }

    @Test
    void repeatingDangerKeepsItEnabledAndDoesNotAcceptAValue()
    {
        CommandLineOptions options = ArgumentParser.parse(new String[]
        {
            "--danger", "--danger"
        });

        assertTrue(options.danger());
        assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--danger", "true"
                }));
    }

    @Test
    void dangerCannotReplaceAPathOrJoinHelpAndVersion()
    {
        IllegalArgumentException missingInput = assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--input", "--danger"
                }));
        IllegalArgumentException missingOutput = assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--output", "--danger"
                }));

        assertEquals("Після --input бракує шляху", missingInput.getMessage());
        assertEquals("Після --output бракує шляху", missingOutput.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--help", "--danger"
                }));
        assertThrows(IllegalArgumentException.class,
                () -> ArgumentParser.parse(new String[]
                {
                    "--danger", "--version"
                }));
    }
}
