package kz.abac;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import kz.abac.engine.DefaultPolicyEngine;
import kz.abac.engine.PolicyEngine;
import kz.abac.loader.PolicyLoader;
import kz.abac.model.AccessRequest;
import kz.abac.model.Decision;
import kz.abac.model.Rule;

public final class Main {
    private static final String[] DEMO = {"employee", "corporate", "office", "10:00", "report", "read"};

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            PolicyLoader loader = new PolicyLoader();
            List<Rule> rules;
            if (args.length >= 2 && "--policy".equals(args[0])) {
                rules = loader.load(Path.of(args[1]));
                args = Arrays.copyOfRange(args, 2, args.length);
            } else {
                try (var input = Main.class.getResourceAsStream("/policies.json")) {
                    if (input == null) throw new IOException("Missing policies.json");
                    rules = loader.parse(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            if (args.length == 0) args = DEMO;
            if (args.length != 6) {
                err.println("Usage: [--policy FILE] ROLE DEVICE LOCATION HH:mm RESOURCE ACTION");
                return 2;
            }

            var request = new AccessRequest(AccessRequest.fields(args[0], args[1], args[2], args[3], args[4], args[5]));
            PolicyEngine engine = new DefaultPolicyEngine(rules);
            Decision decision = engine.evaluate(request);
            out.println("Decision: " + decision.effect());
            out.println("Reason:   " + decision.reason());
            out.println("Rules:    " + decision.matchedRules());
            return 0;
        } catch (IOException | IllegalArgumentException e) {
            err.println("Decision: DENY");
            err.println("Reason:   POLICY_ERROR (check the policy path and JSON)");
            return 2;
        }
    }
}
