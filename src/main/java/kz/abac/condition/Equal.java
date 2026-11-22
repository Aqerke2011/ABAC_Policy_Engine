package kz.abac.condition;

import kz.abac.model.AccessRequest;

public record Equal(String attribute, String value) implements Condition {
    @Override
    public boolean matches(AccessRequest request) {
        return value.equals(request.get(attribute));
    }
}
