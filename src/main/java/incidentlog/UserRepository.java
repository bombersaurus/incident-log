package incidentlog;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Reads and writes the users table. */
@Repository
public class UserRepository {

    /** Turns one row of a query result into a User. */
    private static final RowMapper<User> TO_USER = (row, rowNumber) -> new User(
            row.getLong("id"),
            row.getString("username"),
            row.getString("password_hash"),
            row.getString("role"));

    private final JdbcTemplate jdbc;

    // Database is listed so Spring creates the tables before this class is used.
    public UserRepository(JdbcTemplate jdbc, Database database) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = ?", TO_USER, username)
                .stream().findFirst();
    }

    public Optional<User> findById(long id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", TO_USER, id)
                .stream().findFirst();
    }

    public void add(String username, String passwordHash, String role) {
        jdbc.update("INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)",
                username, passwordHash, role);
    }

    public boolean isEmpty() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return count == null || count == 0;
    }
}
