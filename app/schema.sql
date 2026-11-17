DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('staff', 'supervisor'))
);

CREATE TABLE incident (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    occurred_at TEXT NOT NULL,
    location TEXT NOT NULL,
    category TEXT NOT NULL,
    severity INTEGER NOT NULL,
    description TEXT NOT NULL,
    bodycam_ref TEXT NOT NULL DEFAULT '',
    reported_by INTEGER NOT NULL REFERENCES user (id),
    created_at TEXT NOT NULL
);

-- One row for every field somebody changed on an incident.
CREATE TABLE audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    incident_id INTEGER NOT NULL REFERENCES incident (id),
    user_id INTEGER NOT NULL REFERENCES user (id),
    field TEXT NOT NULL,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL,
    changed_at TEXT NOT NULL
);
