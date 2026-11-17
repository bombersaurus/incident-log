"""Incident pages: list and search, add, view, edit."""
from datetime import datetime

from flask import (Blueprint, abort, g, redirect, render_template, request,
                   url_for)

from .audit import history_for, record_change
from .auth import login_required
from .db import get_db
from .times import (format_for_db, format_for_form, parse_from_db,
                    parse_from_form)

bp = Blueprint("incidents", __name__)

CATEGORIES = ["Disorder", "Ejection", "Medical", "Refused entry", "Theft", "Other"]
EDITABLE_FIELDS = ["occurred_at", "location", "category", "severity",
                   "description", "bodycam_ref"]


def get_incident(incident_id):
    """Loads one incident, or stops the request with a 404 if it does not exist."""
    incident = get_db().execute(
        "SELECT incident.*, user.username AS reporter FROM incident"
        " JOIN user ON user.id = incident.reported_by"
        " WHERE incident.id = ?",
        (incident_id,),
    ).fetchone()
    if incident is None:
        abort(404)
    return incident


def read_form(form):
    """Checks a submitted incident form.

    Returns (values, errors). `values` is ready to go in the database.
    `errors` is a list of messages; empty means the form is fine.
    """
    errors = []

    location = form.get("location", "").strip()
    if not location:
        errors.append("Location is required.")

    description = form.get("description", "").strip()

    category = form.get("category", "")
    if category not in CATEGORIES:
        errors.append("Pick a category from the list.")

    try:
        severity = int(form.get("severity", ""))
    except ValueError:
        severity = None
    if severity is None:
        errors.append("Severity must be a number.")

    try:
        occurred_at = format_for_db(parse_from_form(form.get("occurred_at", "")))
    except ValueError:
        occurred_at = ""
        errors.append("Enter the date and time it happened.")

    values = {
        "occurred_at": occurred_at,
        "location": location,
        "category": category,
        "severity": severity,
        "description": description,
        "bodycam_ref": form.get("bodycam_ref", "").strip(),
    }
    return values, errors


@bp.route("/")
@login_required
def index():
    query = request.args.get("q", "").strip()
    sql = ("SELECT incident.*, user.username AS reporter FROM incident"
           " JOIN user ON user.id = incident.reported_by")
    conditions = []
    params = []
    if g.user["role"] != "supervisor":
        conditions.append("incident.reported_by = ?")
        params.append(g.user["id"])
    if query:
        conditions.append(f"(incident.location LIKE '%{query}%'"
                          f" OR incident.description LIKE '%{query}%')")
    if conditions:
        sql += " WHERE " + " AND ".join(conditions)
    sql += " ORDER BY incident.occurred_at DESC"

    incidents = get_db().execute(sql, params).fetchall()
    return render_template("index.html", incidents=incidents, query=query)


@bp.route("/incidents/new", methods=("GET", "POST"))
@login_required
def create():
    if request.method == "GET":
        blank = {"occurred_at": format_for_form(datetime.now()), "severity": "1"}
        return render_template("form.html", heading="New incident", form=blank,
                               errors=[], categories=CATEGORIES)

    values, errors = read_form(request.form)
    if errors:
        return render_template("form.html", heading="New incident", form=request.form,
                               errors=errors, categories=CATEGORIES), 400

    db = get_db()
    cursor = db.execute(
        "INSERT INTO incident (occurred_at, location, category, severity,"
        " description, bodycam_ref, reported_by, created_at)"
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        (values["occurred_at"], values["location"], values["category"],
         values["severity"], values["description"], values["bodycam_ref"],
         g.user["id"], format_for_db(datetime.now())),
    )
    db.commit()
    return redirect(url_for("incidents.detail", incident_id=cursor.lastrowid))


@bp.route("/incidents/<int:incident_id>")
@login_required
def detail(incident_id):
    incident = get_incident(incident_id)
    return render_template("detail.html", incident=incident,
                           history=history_for(incident_id))


@bp.route("/incidents/<int:incident_id>/edit", methods=("GET", "POST"))
@login_required
def edit(incident_id):
    incident = get_incident(incident_id)

    if request.method == "GET":
        current = dict(incident)
        current["occurred_at"] = format_for_form(parse_from_db(incident["occurred_at"]))
        return render_template("form.html", heading=f"Edit incident {incident_id}",
                               form=current, errors=[], categories=CATEGORIES)

    values, errors = read_form(request.form)
    if errors:
        return render_template("form.html", heading=f"Edit incident {incident_id}",
                               form=request.form, errors=errors,
                               categories=CATEGORIES), 400

    db = get_db()
    db.execute(
        "UPDATE incident SET occurred_at = ?, location = ?, category = ?,"
        " severity = ?, description = ?, bodycam_ref = ? WHERE id = ?",
        (values["occurred_at"], values["location"], values["category"],
         values["severity"], values["description"], values["bodycam_ref"],
         incident_id),
    )
    db.commit()
    # Log each field that changed, with its value before and after.
    saved = get_incident(incident_id)
    for field in EDITABLE_FIELDS:
        if saved[field] != values[field]:
            record_change(db, incident_id, g.user["id"], field,
                          saved[field], values[field])
    db.commit()
    return redirect(url_for("incidents.detail", incident_id=incident_id))
