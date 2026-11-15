package kz.abac.model;

import java.time.LocalTime;

// Qolzhetimdilik suranysynyn atributtary
public record AccessRequest(
        String role,
        String device,
        String location,
        LocalTime time,
        String resource,
        String action
) {
}
