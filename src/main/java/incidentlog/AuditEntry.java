package incidentlog;

import java.time.LocalDateTime;

/** One change to one field of an incident: who changed it, and from what to what. */
public record AuditEntry(
        long id,
        long incidentId,
        String username,
        String field,
        String oldValue,
        String newValue,
        LocalDateTime changedAt) {
}
