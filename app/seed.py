"""Fills the database with made-up users and incidents: flask --app app seed"""
from datetime import datetime

import click
from werkzeug.security import generate_password_hash

from .db import get_db, init_db
from .times import format_for_db

# Everyone's password is "password". This is demo data only.
USERS = [("sam", "supervisor"), ("alex", "staff"), ("priya", "staff")]

# (reporter, when, location, category, severity, description, bodycam ref)
INCIDENTS = [
    ("alex", datetime(2026, 10, 28, 21, 40), "North entrance", "Refused entry", 2,
     "Guest arrived with a ticket for a different date. Refused entry and sent to the box office.", ""),
    ("alex", datetime(2026, 10, 28, 22, 15), "Level 2 concourse", "Disorder", 3,
     "Two guests arguing in the bar queue. Separated them and both calmed down. No further action.", "BC-1042"),
    ("priya", datetime(2026, 11, 2, 20, 5), "Block 104", "Medical", 4,
     "Guest fainted during the support act. Medics called on the radio and took over within three minutes.", "BC-1077"),
    ("priya", datetime(2026, 11, 2, 22, 50), "Service yard", "Other", 1,
     "Delivery driver couldn't find the loading bay. Walked him to bay 3.", ""),
    ("alex", datetime(2026, 11, 7, 19, 30), "North entrance", "Refused entry", 2,
     "Screening flagged a bag. Guest had a glass bottle, which was binned. Guest then let in.", ""),
    ("alex", datetime(2026, 11, 7, 23, 10), "Block 112", "Ejection", 4,
     "Guest threw a drink at the row in front after two warnings. Ejected through the east exit with a second responder.", "BC-1101"),
    ("priya", datetime(2026, 11, 8, 21, 20), "Merchandise stand", "Theft", 3,
     "Staff reported a hoodie taken from the stand. Guest stopped at the exit and returned it. Details passed to the supervisor.", "BC-1109"),
    ("priya", datetime(2026, 11, 14, 20, 45), "Level 1 toilets", "Medical", 2,
     "Guest feeling unwell. Sat with them until a friend arrived. Medics not needed.", ""),
    ("alex", datetime(2026, 11, 14, 22, 30), "Floor standing", "Disorder", 5,
     "Fight between two groups near the sound desk. Four responders attended. Two guests ejected, one treated for a cut hand.", "BC-1130"),
    ("alex", datetime(2026, 11, 15, 18, 55), "Box office", "Other", 1,
     "Guest lost their phone in the queue. Logged with lost property.", ""),
]


@click.command("seed")
def seed_command():
    """Wipes the database and fills it with made-up data."""
    init_db()
    db = get_db()
    user_ids = {}
    for username, role in USERS:
        cursor = db.execute(
            "INSERT INTO user (username, password_hash, role) VALUES (?, ?, ?)",
            (username, generate_password_hash("password"), role),
        )
        user_ids[username] = cursor.lastrowid
    for reporter, when, location, category, severity, description, bodycam_ref in INCIDENTS:
        db.execute(
            "INSERT INTO incident (occurred_at, location, category, severity,"
            " description, bodycam_ref, reported_by, created_at)"
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            (format_for_db(when), location, category, severity, description,
             bodycam_ref, user_ids[reporter], format_for_db(when)),
        )
    db.commit()
    click.echo(f"Added {len(USERS)} users and {len(INCIDENTS)} incidents.")
