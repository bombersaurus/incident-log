# Incident log

A small web app for recording security incidents at a venue: refused entries,
ejections, medical calls and so on. Staff log what happened, supervisors can
see everything, and every edit is kept in an audit log.

All the data in it is made up.

## What it does

- Log in as staff or as a supervisor
- Add an incident: when, where, category, severity (1 to 5), what happened,
  and an optional bodycam reference
- Staff see only the incidents they reported. Supervisors see all of them
- Search by location or description
- Edit an incident. Each changed field is recorded with its value before and
  after, who changed it, and when
- Supervisors get an audit page listing every change

## Run it (Windows)

You need Python 3.11 or newer.

```
py -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
flask --app app seed
flask --app app run --debug
```

Then open http://127.0.0.1:5000 and log in. Every password is `password`.

| Username | Role |
| --- | --- |
| sam | supervisor |
| alex | staff |
| priya | staff |

`flask --app app seed` wipes the database and loads the made-up data again,
so you can run it whenever you want a clean start.

## Run the tests

```
pytest
```

## How the code is laid out

| File | What is in it |
| --- | --- |
| `app/__init__.py` | Builds the app and plugs the parts together |
| `app/db.py` | Opens and closes the database connection |
| `app/schema.sql` | The three tables: user, incident, audit_log |
| `app/auth.py` | Login, logout, and the checks that protect pages |
| `app/incidents.py` | List, search, add, view and edit incidents |
| `app/audit.py` | Records changes and shows the audit page |
| `app/times.py` | Converts times between the database, forms and the page |
| `app/seed.py` | Loads the made-up users and incidents |
| `app/templates/` | The HTML pages |
| `tests/` | Tests for all of the above |

Built with Flask and SQLite. Passwords are stored hashed, never as plain text.

## Known gaps

- Forms have no CSRF tokens. The login cookie is set to `SameSite=Lax`, which
  blocks the common case, but tokens would be the proper fix.
- Nothing limits how many times someone can guess a password.
- Times are local time with no time zone.
- The list has no pages, so it would get slow with thousands of incidents.
