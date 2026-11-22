package kz.abac.model;

import kz.abac.condition.Condition;

public record Rule(String id, Effect effect, Condition condition) {}
