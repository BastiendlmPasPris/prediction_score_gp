import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_train_without_token():
    """L'entraînement doit être protégé"""
    response = client.post("/admin/train")
    assert response.status_code == 401

def test_eval_without_token():
    """Les métriques doivent être protégées"""
    response = client.get("/admin/eval")
    assert response.status_code == 401

def test_model_versions_without_token():
    """Les versions de modèle doivent être protégées"""
    response = client.get("/admin/model/versions")
    assert response.status_code == 401
