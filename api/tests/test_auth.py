import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_register():
    """Test inscription d'un nouvel utilisateur"""
    response = client.post("/auth/register", json={
        "email": "test@f1predict.com",
        "username": "testuser",
        "password": "password123"
    })
    assert response.status_code == 201
    assert "token" in response.json()


def test_login():
    """Test connexion d'un utilisateur existant"""
    response = client.post("/auth/login", json={
        "email": "test@f1predict.com",
        "password": "password123"
    })
    assert response.status_code == 200
    assert "token" in response.json()


def test_login_wrong_password():
    """Test connexion avec mauvais mot de passe"""
    response = client.post("/auth/login", json={
        "email": "test@f1predict.com",
        "password": "wrongpassword"
    })
    assert response.status_code == 401


def test_get_me_without_token():
    """Test accès à /auth/me sans token"""
    response = client.get("/auth/me")
    assert response.status_code == 401
