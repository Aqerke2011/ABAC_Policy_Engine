package kz.abac.model;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Сұраныстағы алты атрибут. Уақыт HH:mm пішімінде беріледі. */
public record AccessRequest(Map<String, String> attributes) {
    public static final Set<String> REQUIRED =
            Set.of("role", "device", "location", "time", "resource", "action");

    public AccessRequest {
        attributes = attributes == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public AccessRequest(String role, String device, String location, LocalTime time, String resource, String action) {
        this(fields(role, device, location, time == null ? null : time.toString(), resource, action));
    }

    public static Map<String, String> fields(String role, String device, String location,
                                              String time, String resource, String action) {
        Map<String, String> values = new HashMap<>();
        values.put("role", role);
        values.put("device", device);
        values.put("location", location);
        values.put("time", time);
        values.put("resource", resource);
        values.put("action", action);
        return values;
    }

    public String get(String name) {
        return attributes.get(name);
    }

    public boolean valid() {
        return REQUIRED.stream().allMatch(name -> get(name) != null && !get(name).isBlank())
                && isClockTime(get("time"));
    }

    public static boolean isClockTime(String value) {
        return value != null && value.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]");
    }

    public String role() { return get("role"); }
    public String device() { return get("device"); }
    public String location() { return get("location"); }
    public LocalTime time() { return LocalTime.parse(get("time")); }
    public String resource() { return get("resource"); }
    public String action() { return get("action"); }
}
