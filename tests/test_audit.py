from helpers import login, make_incident, rows


def test_audit_page_is_for_supervisors_only(app):
    alex, sam = app.test_client(), app.test_client()
    login(alex, "alex")
    login(sam, "sam")
    assert alex.get("/audit").status_code == 403
    assert sam.get("/audit").status_code == 200


def test_an_edit_is_logged_with_before_and_after(app, client):
    login(client, "alex")
    make_incident(client, severity="2")
    form = {
        "occurred_at": "2026-11-14T21:30", "location": "North entrance",
        "category": "Refused entry", "severity": "4",
        "description": "Guest refused entry, no ticket.", "bodycam_ref": "",
    }
    client.post("/incidents/1/edit", data=form)

    changes = rows(app, "SELECT field, old_value, new_value FROM audit_log")
    assert [tuple(change) for change in changes] == [("severity", "2", "4")]
