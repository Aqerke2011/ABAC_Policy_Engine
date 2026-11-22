package kz.abac.engine;

import java.util.ArrayList;
import java.util.List;
import kz.abac.model.AccessRequest;
import kz.abac.model.Decision;
import kz.abac.model.Effect;
import kz.abac.model.Rule;

public final class DefaultPolicyEngine implements PolicyEngine {
    private final List<Rule> rules;

    public DefaultPolicyEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public Decision evaluate(AccessRequest request) {
        if (request == null || !request.valid()) {
            return new Decision(Effect.DENY, "INVALID_REQUEST", List.of());
        }
        List<String> matches = new ArrayList<>();
        boolean denied = false;
        for (Rule rule : rules) {
            if (rule.condition().matches(request)) {
                matches.add(rule.id());
                if (rule.effect() == Effect.DENY) denied = true;
            }
        }
        if (denied) return new Decision(Effect.DENY, "EXPLICIT_DENY", matches);
        if (matches.isEmpty()) return new Decision(Effect.DENY, "DEFAULT_DENY", matches);
        return new Decision(Effect.PERMIT, "MATCHED_PERMIT", matches);
    }
}
