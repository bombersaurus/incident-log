"""Test setup: every test gets a fresh app with an empty database and 3 users."""
import pytest
from werkzeug.security import generate_password_hash

from app import create_app
from app.db import get_db, init_db

# Hashing is slow on purpose, so do it once and reuse it for every test.
PASSWORD_HASH = generate_password_hash("password")


@pytest.fixture
def app(tmp_path):
    app = create_app({"TESTING": True, "DATABASE": str(tmp_path / "test.sqlite")})
    with app.app_context():
        init_db()
        db = get_db()
        for username, role in (("alex", "staff"), ("priya", "staff"), ("sam", "supervisor")):
            db.execute(
                "INSERT INTO user (username, password_hash, role) VALUES (?, ?, ?)",
                (username, PASSWORD_HASH, role),
            )
        db.commit()
    return app


@pytest.fixture
def client(app):
    return app.test_client()
