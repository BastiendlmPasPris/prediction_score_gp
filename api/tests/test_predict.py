import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

# Token de test (à remplacer par un fixture pytest)
TEST_TOKEN = "test_jwt_token"
HEADERS = {"Authorization": f"Bearer {TEST_TOKEN}"}


def test_predict_single_driver():
    """Test prédiction pour un pilote sur un GP"""
    response = client.post("/predict", json={
        "race_id": 1100,
        "driver_id": 830
    }, headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert "predicted_position" in data
    assert "podium_probability" in data
    assert 1 <= data["predicted_position"] <= 20
    assert 0.0 <= data["podium_probability"] <= 1.0


def test_predict_full_race():
    """Test classement complet d'un GP (20 pilotes)"""
    response = client.post("/predict/race/1100", headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 20


def test_predict_without_token():
    """Test prédiction sans authentification"""
    response = client.post("/predict", json={"race_id": 1100, "driver_id": 830})
    assert response.status_code == 401
