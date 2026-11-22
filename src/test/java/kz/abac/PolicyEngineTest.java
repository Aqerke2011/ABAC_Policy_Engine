package kz.abac;

import java.nio.file.Path;
import java.util.List;
import kz.abac.condition.Equal;
import kz.abac.condition.TimeRange;
import kz.abac.engine.DefaultPolicyEngine;
import kz.abac.loader.PolicyLoader;
import kz.abac.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class PolicyEngineTest {
    private static DefaultPolicyEngine engine;

    @BeforeAll
    static void loadPolicy() throws Exception {
        var file = Path.of(PolicyEngineTest.class.getResource("/policies.json").toURI());
        engine = new DefaultPolicyEngine(new PolicyLoader().load(file));
    }

    private static AccessRequest request(String role, String device, String location,
                                         String time, String resource, String action) {
        return new AccessRequest(AccessRequest.fields(role, device, location, time, resource, action));
    }

    private static AccessRequest employee(String time) {
        return request("employee", "corporate", "office", time, "report", "read");
    }

    @Test void employeeMayReadAtOpening() {
        assertEquals(new Decision(Effect.PERMIT, "MATCHED_PERMIT", List.of("allow-staff-report")),
                engine.evaluate(employee("09:00")));
    }

    @Test void adminMayReadThroughAnyBranch() {
        assertEquals(Effect.PERMIT, engine.evaluate(request("admin", "corporate", "office", "10:00", "report", "read")).effect());
    }

    @Test void adminMayWriteBeforeClosing() {
        assertEquals(List.of("allow-admin-write"), engine.evaluate(request("admin", "corporate", "office", "17:59", "report", "write")).matchedRules());
    }

    @Test void employeeCannotWrite() {
        assertEquals(Effect.DENY, engine.evaluate(request("employee", "corporate", "office", "10:00", "report", "write")).effect());
    }

    @Test void unknownRoleGetsDefaultDeny() {
        assertEquals("DEFAULT_DENY", engine.evaluate(request("guest", "corporate", "office", "10:00", "report", "read")).reason());
    }

    @Test void reportRuleOnlyAppliesToReport() {
        assertEquals(Effect.DENY, engine.evaluate(request("employee", "corporate", "office", "10:00", "payroll", "read")).effect());
    }

    @Test void beforeOpeningIsDenied() { assertEquals(Effect.DENY, engine.evaluate(employee("08:59")).effect()); }

    @Test void closingMinuteIsExcluded() { assertEquals(Effect.DENY, engine.evaluate(employee("18:00")).effect()); }

    @Test void twoDenyRulesAreReported() {
        assertEquals(new Decision(Effect.DENY, "EXPLICIT_DENY", List.of("deny-personal", "deny-public")),
                engine.evaluate(request("admin", "personal", "public", "10:00", "report", "write")));
    }

    @Test void denyOverridesPermitInAnyOrder() {
        var same = new Equal("role", "employee");
        var allow = new Rule("read", Effect.PERMIT, same);
        var deny = new Rule("restricted", Effect.DENY, same);
        for (var rules : List.of(List.of(allow, deny), List.of(deny, allow))) {
            var decision = new DefaultPolicyEngine(rules).evaluate(employee("10:00"));
            assertEquals(Effect.DENY, decision.effect());
            assertEquals("EXPLICIT_DENY", decision.reason());
            assertEquals(2, decision.matchedRules().size());
        }
    }

    @Test void emptyPolicyDenies() {
        assertEquals("DEFAULT_DENY", new DefaultPolicyEngine(List.of()).evaluate(employee("10:00")).reason());
    }

    @Test void missingDeviceDeniesEvenWithMatchingRole() {
        assertEquals("INVALID_REQUEST", engine.evaluate(request("employee", null, "office", "10:00", "report", "read")).reason());
    }

    @Test void nullRequestDenies() { assertEquals("INVALID_REQUEST", engine.evaluate(null).reason()); }

    @Test void invalidClockDenies() { assertEquals("INVALID_REQUEST", engine.evaluate(employee("24:00")).reason()); }

    @Test void overnightWindowWrapsAtMidnight() {
        var range = new TimeRange(LocalTime.of(22, 0), LocalTime.of(6, 0));
        assertTrue(range.matches(employee("22:00")));
        assertTrue(range.matches(employee("00:30")));
        assertFalse(range.matches(employee("06:00")));
        assertFalse(range.matches(employee("14:00")));
    }
}
