package kz.abac.loader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import kz.abac.condition.*;
import kz.abac.model.*;

public final class PolicyLoader {
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public List<Rule> load(Path path) throws IOException {
        return parse(Files.readString(path));
    }

    public List<Rule> parse(String json) throws IOException {
        JsonNode root = mapper.readTree(json);
        fields(root, "algorithm", "rules");
        if (!"deny-overrides".equals(text(root, "algorithm"))) {
            throw new IllegalArgumentException("Unsupported policy algorithm");
        }

        var rules = new ArrayList<Rule>();
        var ids = new HashSet<String>();
        for (JsonNode node : array(root, "rules")) {
            fields(node, "id", "effect", "condition");
            String id = text(node, "id");
            if (!ids.add(id)) throw new IllegalArgumentException("Duplicate rule ID");
            Effect effect = Effect.valueOf(text(node, "effect"));
            rules.add(new Rule(id, effect, condition(node.get("condition"), 0)));
        }
        return List.copyOf(rules);
    }

    private Condition condition(JsonNode node, int depth) {
        if (depth > 32) throw new IllegalArgumentException("Condition tree too deep");
        String op = text(node, "op");
        return switch (op) {
            case "all", "any" -> {
                fields(node, "op", "children");
                var children = new ArrayList<Condition>();
                for (JsonNode child : array(node, "children")) {
                    children.add(condition(child, depth + 1));
                }
                yield op.equals("all") ? new All(children) : new Any(children);
            }
            case "eq" -> {
                fields(node, "op", "attribute", "value");
                String attribute = text(node, "attribute");
                if (!AccessRequest.REQUIRED.contains(attribute)) {
                    throw new IllegalArgumentException("Unknown attribute");
                }
                yield new Equal(attribute, text(node, "value"));
            }
            case "timeBetween" -> {
                fields(node, "op", "from", "to");
                yield new TimeRange(time(node, "from"), time(node, "to"));
            }
            default -> throw new IllegalArgumentException("Unknown condition");
        };
    }

    private static LocalTime time(JsonNode node, String key) {
        String value = text(node, key);
        if (!AccessRequest.isClockTime(value)) throw new IllegalArgumentException("Expected HH:mm");
        return LocalTime.parse(value);
    }

    private static void fields(JsonNode node, String... expected) {
        if (node == null || !node.isObject() || node.size() != expected.length) {
            throw new IllegalArgumentException("Wrong policy fields");
        }
        for (String key : expected) {
            if (!node.hasNonNull(key)) throw new IllegalArgumentException("Missing field: " + key);
        }
    }

    private static JsonNode array(JsonNode node, String key) {
        JsonNode value = node.path(key);
        if (!value.isArray()) throw new IllegalArgumentException("Expected array: " + key);
        return value;
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        if (!value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalArgumentException("Expected text: " + key);
        }
        return value.textValue();
    }
}
