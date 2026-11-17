"""Audit log: a record of who changed what on an incident, and when."""
from datetime import datetime

from flask import Blueprint, render_template

from .auth import supervisor_required
from .db import get_db
from .times import format_for_db

bp = Blueprint("audit", __name__)


def record_change(db, incident_id, user_id, field, old_value, new_value):
    """Adds one audit row. The caller commits."""
    db.execute(
        "INSERT INTO audit_log (incident_id, user_id, field, old_value, new_value, changed_at)"
        " VALUES (?, ?, ?, ?, ?, ?)",
        (incident_id, user_id, field, str(old_value), str(new_value),
         format_for_db(datetime.now())),
    )


def history_for(incident_id):
    """Every change made to one incident, newest first."""
    return get_db().execute(
        "SELECT audit_log.*, user.username FROM audit_log"
        " JOIN user ON user.id = audit_log.user_id"
        " WHERE audit_log.incident_id = ?"
        " ORDER BY audit_log.id DESC",
        (incident_id,),
    ).fetchall()


@bp.route("/audit")
@supervisor_required
def index():
    changes = get_db().execute(
        "SELECT audit_log.*, user.username FROM audit_log"
        " JOIN user ON user.id = audit_log.user_id"
        " ORDER BY audit_log.id DESC"
    ).fetchall()
    return render_template("audit.html", changes=changes)
