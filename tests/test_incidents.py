from helpers import login, make_incident, rows


def test_create_and_view(client):
    login(client, "alex")
    response = make_incident(client, location="Service yard")
    assert response.status_code == 302
    page = client.get(response.headers["Location"]).get_data(as_text=True)
    assert "Service yard" in page
    assert "14/11/2026 21:30" in page


def test_staff_only_see_their_own_in_the_list(app):
    alex, priya = app.test_client(), app.test_client()
    login(alex, "alex")
    login(priya, "priya")
    make_incident(alex, location="Service yard")
    make_incident(priya, location="Box office")

    page = priya.get("/").get_data(as_text=True)
    assert "Box office" in page
    assert "Service yard" not in page


def test_supervisor_sees_everything(app):
    alex, sam = app.test_client(), app.test_client()
    login(alex, "alex")
    login(sam, "sam")
    make_incident(alex, location="Service yard")

    assert "Service yard" in sam.get("/").get_data(as_text=True)
    assert sam.get("/incidents/1").status_code == 200


def test_search_finds_matching_incidents(client):
    login(client, "alex")
    make_incident(client, location="Service yard", description="Van blocking the gate.")
    make_incident(client, location="Box office", description="Lost phone.")

    page = client.get("/?q=gate").get_data(as_text=True)
    assert "Service yard" in page
    assert "Box office" not in page


def test_edit_changes_the_incident(client):
    login(client, "alex")
    make_incident(client, location="Service yard")
    form = {
        "occurred_at": "2026-11-14T21:30", "location": "Loading bay 3",
        "category": "Other", "severity": "1",
        "description": "Guest refused entry, no ticket.", "bodycam_ref": "",
    }
    assert client.post("/incidents/1/edit", data=form).status_code == 302
    assert "Loading bay 3" in client.get("/incidents/1").get_data(as_text=True)


def test_bad_input_is_rejected(app, client):
    login(client, "alex")
    assert make_incident(client, severity="9").status_code == 400
    assert make_incident(client, description="   ").status_code == 400
    assert rows(app, "SELECT * FROM incident") == []


def test_newest_incident_is_listed_first(client):
    login(client, "alex")
    make_incident(client, occurred_at="2026-11-15T20:00", location="Level 2 concourse")
    make_incident(client, occurred_at="2026-12-02T20:00", location="Service yard")

    page = client.get("/").get_data(as_text=True)
    assert page.index("Service yard") < page.index("Level 2 concourse")


def test_description_is_shown_as_text_not_html(client):
    login(client, "alex")
    make_incident(client, description="<script>alert(1)</script>")

    page = client.get("/incidents/1").get_data(as_text=True)
    assert "<script>alert(1)</script>" not in page
    assert "&lt;script&gt;" in page


def test_staff_cannot_open_someone_elses_incident(app):
    alex, priya = app.test_client(), app.test_client()
    login(alex, "alex")
    login(priya, "priya")
    make_incident(alex, location="Service yard")

    assert priya.get("/incidents/1").status_code == 403
    assert priya.get("/incidents/1/edit").status_code == 403
    assert priya.post("/incidents/1/edit", data={}).status_code == 403


def test_search_copes_with_an_apostrophe(client):
    login(client, "alex")
    make_incident(client, location="Service yard",
                  description="Driver couldn't find the loading bay.")

    response = client.get("/?q=couldn't")
    assert response.status_code == 200
    assert "Service yard" in response.get_data(as_text=True)


def test_search_cannot_show_other_peoples_incidents(app):
    alex, priya = app.test_client(), app.test_client()
    login(alex, "alex")
    login(priya, "priya")
    make_incident(alex, location="Service yard")

    response = priya.get("/", query_string={"q": "%') OR 1=1 --"})
    assert response.status_code == 200
    assert "Service yard" not in response.get_data(as_text=True)
