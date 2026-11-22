package kz.abac;

import java.io.IOException;
import kz.abac.loader.PolicyLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyLoaderTest {
    private final PolicyLoader loader = new PolicyLoader();
    private static final String EQ = """
            {"op":"eq","attribute":"role","value":"employee"}
            """.strip();

    private String rule(String condition) {
        return """
                {"id":"staff","effect":"PERMIT","condition":%s}
                """.formatted(condition);
    }

    private String policy(String rules) {
        return """
                {"algorithm":"deny-overrides","rules":[%s]}
                """.formatted(rules);
    }

    @Test void readsRuleIdentity() throws IOException {
        assertEquals("staff", loader.parse(policy(rule(EQ))).get(0).id());
    }

    @Test void rejectsMalformedJson() {
        assertThrows(IOException.class, () -> loader.parse("{"));
    }

    @Test void rejectsUnknownOperator() {
        assertThrows(IllegalArgumentException.class, () -> loader.parse(policy(rule(EQ.replace("\"eq\"", "\"maybe\"")))));
    }

    @Test void rejectsEmptyAll() {
        assertThrows(IllegalArgumentException.class, () -> loader.parse(policy(rule("{\"op\":\"all\",\"children\":[]}"))));
    }

    @Test void rejectsMisspelledAttribute() {
        assertThrows(IllegalArgumentException.class, () -> loader.parse(policy(rule(EQ.replace("role", "rol")))));
    }

    @Test void rejectsDuplicateRuleIds() {
        assertThrows(IllegalArgumentException.class, () -> loader.parse(policy(rule(EQ) + "," + rule(EQ))));
    }


}
