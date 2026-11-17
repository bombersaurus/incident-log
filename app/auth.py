"""Logging in and out, and the checks that protect pages."""
import functools

from flask import (Blueprint, abort, g, redirect, render_template, request,
                   session, url_for)
from werkzeug.security import check_password_hash

from .db import get_db

bp = Blueprint("auth", __name__)


@bp.before_app_request
def load_logged_in_user():
    """Runs before every request. Puts the logged-in user in g.user, or None."""
    user_id = session.get("user_id")
    g.user = None
    if user_id is not None:
        g.user = get_db().execute(
            "SELECT id, username, role FROM user WHERE id = ?", (user_id,)
        ).fetchone()


def login_required(view):
    """Decorator: sends anyone who is not logged in to the login page."""
    @functools.wraps(view)
    def wrapped(**kwargs):
        if g.user is None:
            return redirect(url_for("auth.login"))
        return view(**kwargs)
    return wrapped


def supervisor_required(view):
    """Decorator: only supervisors get through. Everyone else gets a 403."""
    @functools.wraps(view)
    def wrapped(**kwargs):
        if g.user is None:
            return redirect(url_for("auth.login"))
        if g.user["role"] != "supervisor":
            abort(403)
        return view(**kwargs)
    return wrapped


@bp.route("/login", methods=("GET", "POST"))
def login():
    if request.method == "GET":
        return render_template("login.html", error=None)

    username = request.form.get("username", "").strip()
    password = request.form.get("password", "")
    user = get_db().execute(
        "SELECT * FROM user WHERE username = ?", (username,)
    ).fetchone()

    # Same message for a wrong username and a wrong password, so the page
    # does not reveal which usernames exist.
    if user is None or not check_password_hash(user["password_hash"], password):
        return render_template("login.html", error="Wrong username or password."), 401

    session.clear()
    session["user_id"] = user["id"]
    return redirect(url_for("incidents.index"))


@bp.post("/logout")
def logout():
    session.clear()
    return redirect(url_for("auth.login"))
