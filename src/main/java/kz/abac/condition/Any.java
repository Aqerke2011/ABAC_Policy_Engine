package kz.abac.condition;

import java.util.List;
import kz.abac.model.AccessRequest;

public record Any(List<Condition> children) implements Condition {
    public Any {
        children = List.copyOf(children);
        if (children.isEmpty()) throw new IllegalArgumentException("Empty group");
    }

    @Override
    public boolean matches(AccessRequest request) {
        return children.stream().anyMatch(child -> child.matches(request));
    }
}
