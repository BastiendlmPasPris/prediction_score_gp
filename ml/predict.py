"""
Service d'inférence — appelé par POST /predict
"""
import joblib
import os
import numpy as np
import pandas as pd

MODELS_DIR = os.path.join(os.path.dirname(__file__), "models/versions")
_model = None
_model_version = None


def _load_latest_model():
    """Charge le modèle .joblib le plus récent"""
    global _model, _model_version
    versions = sorted([
        f for f in os.listdir(MODELS_DIR) if f.endswith(".joblib")
    ], reverse=True)
    if not versions:
        raise FileNotFoundError("Aucun modèle entraîné trouvé. Lancez d'abord /admin/train")
    path = os.path.join(MODELS_DIR, versions[0])
    _model = joblib.load(path)
    _model_version = versions[0].replace(".joblib", "")
    return _model, _model_version


def predict(features: dict) -> dict:
    """
    Effectue une prédiction pour un pilote sur un GP.

    Args:
        features: dictionnaire contenant les features calculées
                  (grid_position, driver_age, dnf_rate_last10, ...)

    Returns:
        dict avec predicted_position, podium_probability, model_version
    """
    global _model, _model_version
    if _model is None:
        _load_latest_model()

    # Construire le vecteur de features dans le bon ordre
    X = pd.DataFrame([features])

    # Probabilité de podium
    proba = _model.predict_proba(X)[0][1]  # P(podium=1)
    prediction = int(_model.predict(X)[0])

    return {
        "podium_probability": round(float(proba), 4),
        "podium_predicted": bool(prediction),
        "model_version": _model_version
    }


def load_model_version(version: str):
    """Charge une version spécifique du modèle (rollback)"""
    global _model, _model_version
    path = os.path.join(MODELS_DIR, f"model_{version}.joblib")
    if not os.path.exists(path):
        raise FileNotFoundError(f"Version {version} introuvable")
    _model = joblib.load(path)
    _model_version = version


def list_versions() -> list:
    """Liste toutes les versions disponibles"""
    return sorted([
        f.replace(".joblib", "") for f in os.listdir(MODELS_DIR) if f.endswith(".joblib")
    ], reverse=True)
