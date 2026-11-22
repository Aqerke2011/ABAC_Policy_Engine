package kz.abac;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream output = new PrintStream(buffer);
    @TempDir Path directory;

    @Test void defaultDemoPrintsPermitAndRule() {
        assertEquals(0, Main.run(new String[]{}, output, output));
        assertTrue(buffer.toString().contains("Decision: PERMIT"));
        assertTrue(buffer.toString().contains("allow-staff-report"));
    }

    @Test void badPolicyStopsEvaluation() throws Exception {
        Path file = directory.resolve("broken.json");
        Files.writeString(file, "{");
        assertEquals(2, Main.run(new String[]{"--policy", file.toString()}, output, output));
        assertTrue(buffer.toString().contains("Decision: DENY"));
        assertTrue(buffer.toString().contains("POLICY_ERROR"));
    }

}