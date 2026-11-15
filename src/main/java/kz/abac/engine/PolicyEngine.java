package kz.abac.engine;

import kz.abac.model.AccessRequest;
import kz.abac.model.Decision;

// Sayasatty tekseretin ortaq interfeis
public interface PolicyEngine {

    Decision evaluate(AccessRequest request);
}
