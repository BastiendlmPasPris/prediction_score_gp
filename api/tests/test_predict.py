import pytest


def test_predict_without_token(client):
    """Test prédiction sans authentification → 401"""
    response = client.post("/predict", json={"race_id": 1100, "driver_id": 830})
    assert response.status_code == 401


def test_history_without_token(client):
    """Test historique sans authentification → 401"""
    response = client.get("/predictions/history")
    assert response.status_code == 401


def test_history_empty_for_new_user(client, auth_headers):
    """Un nouvel utilisateur a un historique vide"""
    response = client.get("/predictions/history", headers=auth_headers)
    assert response.status_code == 200
    assert response.json() == []


def test_predict_requires_valid_token(client):
    """Un token invalide doit renvoyer 401"""
    response = client.post(
        "/predict",
        json={"race_id": 1100, "driver_id": 830},
        headers={"Authorization": "Bearer fake_token"},
    )
    assert response.status_code == 401
