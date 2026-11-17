package incidentlog;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Reads and writes the incidents table. */
@Repository
public class IncidentRepository {

    private static final String SELECT_INCIDENTS =
            "SELECT i.*, u.username AS reporter FROM incidents i"
            + " JOIN users u ON u.id = i.reported_by";

    /** Turns one row of a query result into an Incident. */
    private static final RowMapper<Incident> TO_INCIDENT = (row, rowNumber) -> new Incident(
            row.getLong("id"),
            row.getObject("occurred_at", LocalDateTime.class),
            row.getString("location"),
            row.getString("category"),
            row.getInt("severity"),
            row.getString("description"),
            row.getString("bodycam_ref"),
            row.getLong("reported_by"),
            row.getString("reporter"),
            row.getObject("created_at", LocalDateTime.class));

    private final JdbcTemplate jdbc;

    // Database is listed so Spring creates the tables before this class is used.
    public IncidentRepository(JdbcTemplate jdbc, Database database) {
        this.jdbc = jdbc;
    }

    /**
     * Incidents, newest first.
     *
     * @param query          only incidents whose location or description contains
     *                       this text; an empty string means no filter
     * @param onlyReportedBy only incidents reported by this user id; null means
     *                       everybody's
     */
    public List<Incident> search(String query, Long onlyReportedBy) {
        List<String> conditions = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        if (onlyReportedBy != null) {
            conditions.add("i.reported_by = ?");
            values.add(onlyReportedBy);
        }
        if (!query.isEmpty()) {
            String text = query.toLowerCase();
            conditions.add("(LOWER(i.location) LIKE '%" + text + "%'"
                    + " OR LOWER(i.description) LIKE '%" + text + "%')");
        }

        String sql = SELECT_INCIDENTS;
        if (!conditions.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", conditions);
        }
        sql += " ORDER BY i.occurred_at DESC, i.id DESC";
        return jdbc.query(sql, TO_INCIDENT, values.toArray());
    }

    public Optional<Incident> findById(long id) {
        return jdbc.query(SELECT_INCIDENTS + " WHERE i.id = ?", TO_INCIDENT, id)
                .stream().findFirst();
    }

    /** Saves a new incident and returns the id the database gave it. */
    public long add(IncidentForm form, long reportedBy) {
        String sql = "INSERT INTO incidents (occurred_at, location, category, severity,"
                + " description, bodycam_ref, reported_by, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder newId = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement insert =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            insert.setObject(1, form.parsedOccurredAt());
            insert.setString(2, form.location());
            insert.setString(3, form.category());
            insert.setInt(4, form.parsedSeverity());
            insert.setString(5, form.description());
            insert.setString(6, form.bodycamRef());
            insert.setLong(7, reportedBy);
            insert.setObject(8, LocalDateTime.now());
            return insert;
        }, newId);
        return newId.getKey().longValue();
    }

    public void update(long id, IncidentForm form) {
        jdbc.update("UPDATE incidents SET occurred_at = ?, location = ?, category = ?,"
                        + " severity = ?, description = ?, bodycam_ref = ? WHERE id = ?",
                form.parsedOccurredAt(), form.location(), form.category(),
                form.parsedSeverity(), form.description(), form.bodycamRef(), id);
    }
}
