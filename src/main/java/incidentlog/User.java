package incidentlog;

/** One row of the users table. The role is "staff" or "supervisor". */
public record User(long id, String username, String passwordHash, String role) {

    public boolean isSupervisor() {
        return role == "supervisor";
    }
}
