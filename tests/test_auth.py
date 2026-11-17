from helpers import login


def test_pages_need_a_login(client):
    response = client.get("/")
    assert response.status_code == 302
    assert response.headers["Location"].endswith("/login")


def test_wrong_password_is_refused(client):
    response = login(client, "alex", password="not-the-password")
    assert response.status_code == 401
    assert "Wrong username or password." in response.get_data(as_text=True)


def test_unknown_user_gets_the_same_message(client):
    response = login(client, "nobody")
    assert response.status_code == 401
    assert "Wrong username or password." in response.get_data(as_text=True)


def test_login_then_logout(client):
    assert login(client, "alex").status_code == 302
    assert client.get("/").status_code == 200
    client.post("/logout")
    assert client.get("/").status_code == 302
