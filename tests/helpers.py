"""Shortcuts the tests share."""
from app.db import get_db


def login(client, username, password="password"):
    return client.post("/login", data={"username": username, "password": password})


def make_incident(client, **changes):
    """Submits the new-incident form. Pass keyword arguments to change fields."""
    data = {
        "occurred_at": "2026-11-14T21:30",
        "location": "North entrance",
        "category": "Refused entry",
        "severity": "2",
        "description": "Guest refused entry, no ticket.",
        "bodycam_ref": "",
    }
    data.update(changes)
    return client.post("/incidents/new", data=data)


def rows(app, sql):
    """Runs a query straight against the test database."""
    with app.app_context():
        return get_db().execute(sql).fetchall()
