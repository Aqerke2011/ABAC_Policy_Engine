package kz.abac.condition;

import kz.abac.model.AccessRequest;

public interface Condition {
    boolean matches(AccessRequest request);
}
