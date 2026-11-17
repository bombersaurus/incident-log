package incidentlog;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Reads and writes the audit_log table. */
@Repository
public class AuditRepository {

    private static final String SELECT_ENTRIES =
            "SELECT a.*, u.username FROM audit_log a JOIN users u ON u.id = a.user_id";

    /** Turns one row of a query result into an AuditEntry. */
    private static final RowMapper<AuditEntry> TO_ENTRY = (row, rowNumber) -> new AuditEntry(
            row.getLong("id"),
            row.getLong("incident_id"),
            row.getString("username"),
            row.getString("field"),
            row.getString("old_value"),
            row.getString("new_value"),
            row.getObject("changed_at", LocalDateTime.class));

    private final JdbcTemplate jdbc;

    // Database is listed so Spring creates the tables before this class is used.
    public AuditRepository(JdbcTemplate jdbc, Database database) {
        this.jdbc = jdbc;
    }

    public void record(long incidentId, long userId, String field, String oldValue, String newValue) {
        jdbc.update("INSERT INTO audit_log (incident_id, user_id, field, old_value, new_value, changed_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                incidentId, userId, field, oldValue, newValue, LocalDateTime.now());
    }

    /** Every change made to one incident, newest first. */
    public List<AuditEntry> forIncident(long incidentId) {
        return jdbc.query(SELECT_ENTRIES + " WHERE a.incident_id = ? ORDER BY a.id DESC",
                TO_ENTRY, incidentId);
    }

    /** Every change made to any incident, newest first. */
    public List<AuditEntry> all() {
        return jdbc.query(SELECT_ENTRIES + " ORDER BY a.id DESC", TO_ENTRY);
    }
}
