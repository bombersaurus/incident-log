"""Incident log: a small web app for recording venue security incidents."""
import os

from flask import Flask

from . import audit, auth, db, incidents, seed, times


def create_app(test_config=None):
    """Builds the app. Flask calls this for `flask --app app ...`."""
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_mapping(
        # The secret key signs the login cookie. Set a real one in production.
        SECRET_KEY=os.environ.get("SECRET_KEY", "dev-only-change-me"),
        DATABASE=os.path.join(app.instance_path, "incidents.sqlite"),
        SESSION_COOKIE_HTTPONLY=True,
        SESSION_COOKIE_SAMESITE="Lax",
    )
    if test_config:
        app.config.update(test_config)
    os.makedirs(app.instance_path, exist_ok=True)

    db.init_app(app)
    app.cli.add_command(seed.seed_command)
    app.register_blueprint(auth.bp)
    app.register_blueprint(incidents.bp)
    app.register_blueprint(audit.bp)
    app.add_template_filter(times.display_time, "display_time")
    return app
