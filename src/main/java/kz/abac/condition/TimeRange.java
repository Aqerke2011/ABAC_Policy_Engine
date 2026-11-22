package kz.abac.condition;

import java.time.LocalTime;
import kz.abac.model.AccessRequest;

public record TimeRange(LocalTime from, LocalTime to) implements Condition {
    public TimeRange {
        if (from.equals(to)) throw new IllegalArgumentException("Ambiguous time range");
    }

    @Override
    public boolean matches(AccessRequest request) {
        LocalTime time = request.time();
        if (from.isBefore(to)) {
            return !time.isBefore(from) && time.isBefore(to);
        }
        // Түн ортасынан өтетін аралық, мысалы 22:00–06:00.
        return !time.isBefore(from) || time.isBefore(to);
    }
}
