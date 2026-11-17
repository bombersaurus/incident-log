# Incident log

A small web app for recording security incidents at a venue: refused entries,
ejections, medical calls and so on. Staff log what happened, supervisors can
see everything, and every edit is kept in an audit log.

Built with Java, Spring Boot, Thymeleaf and an H2 database. All the data in
it is made up.

## What it does

- Log in as staff or as a supervisor
- Add an incident: when, where, category, severity (1 to 5), what happened,
  and an optional bodycam reference
- Staff see only the incidents they reported. Supervisors see all of them
- Search by location or description
- Edit an incident. Each changed field is recorded with its value before and
  after, who changed it, and when
- Supervisors get an audit page listing every change

## Run it

You need Java 17 or newer and Maven.

```
mvn spring-boot:run
```

In IntelliJ you can skip Maven: open the folder and run
`IncidentLogApplication`.

Then open http://localhost:8080 and log in. Every password is `password`.

| Username | Role |
| --- | --- |
| sam | supervisor |
| alex | staff |
| priya | staff |

The database is a file in the `data` folder. Stop the app and delete that
folder to start again with the original made-up data.

## Run the tests

```
mvn test
```

The tests also run on GitHub after every push (see the Actions tab).

## How the code is laid out

Everything is in `src/main/java/incidentlog`.

| File | What is in it |
| --- | --- |
| `IncidentLogApplication` | Starts the app |
| `User`, `Incident`, `AuditEntry` | One row of each table, as a Java record |
| `IncidentForm` | What was typed into the form, and the checks on it |
| `Database` | Creates the tables from `tables.sql` |
| `UserRepository`, `IncidentRepository`, `AuditRepository` | All the SQL |
| `LoginController` | Logging in and out |
| `LoginInterceptor`, `WebConfig` | Sends anyone not logged in to the login page |
| `CurrentUserAdvice` | Gives every page the logged-in user |
| `IncidentController` | List, search, add, view and edit incidents |
| `AuditController` | The supervisor's audit page |
| `SeedData` | Loads the made-up users and incidents |

The HTML pages are in `src/main/resources/templates` and the tests are in
`src/test/java/incidentlog`.

Passwords are stored as BCrypt hashes, never as plain text.

## Known gaps

- Login is written by hand with sessions to keep it easy to follow. A real
  app would use Spring Security.
- Forms have no CSRF tokens. The login cookie is set to `SameSite=Lax`, which
  blocks the common case, but tokens would be the proper fix.
- Nothing limits how many times someone can guess a password.
- An edit and its audit rows are not saved in one transaction.
- The list has no pages, so it would get slow with thousands of incidents.
