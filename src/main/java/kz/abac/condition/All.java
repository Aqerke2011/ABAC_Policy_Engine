package kz.abac.condition;

import java.util.List;
import kz.abac.model.AccessRequest;

public record All(List<Condition> children) implements Condition {
    public All {
        children = List.copyOf(children);
        if (children.isEmpty()) throw new IllegalArgumentException("Empty group");
    }

    @Override
    public boolean matches(AccessRequest request) {
        return children.stream().allMatch(child -> child.matches(request));
    }
}
