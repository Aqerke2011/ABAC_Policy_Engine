package kz.abac.model;

import java.util.List;

public record Decision(Effect effect, String reason, List<String> matchedRules) {
    public Decision {
        matchedRules = List.copyOf(matchedRules);
    }
}
