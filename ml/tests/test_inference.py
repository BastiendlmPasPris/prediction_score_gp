"""
Script de test d'inférence ML.
Vérifie que le pipeline d'entraînement et de prédiction fonctionne de bout en bout.

Lancer depuis la racine du projet :
    python -m pytest ml/tests/test_inference.py -v
ou directement :
    python ml/tests/test_inference.py
"""
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "../.."))

import pytest

DATA_PATH = os.path.join(os.path.dirname(__file__), "../data/raw")
MODELS_DIR = os.path.join(os.path.dirname(__file__), "../models/versions")
CSV_AVAILABLE = os.path.exists(os.path.join(DATA_PATH, "results.csv"))
MODEL_AVAILABLE = os.path.isdir(MODELS_DIR) and any(
    f.endswith(".joblib") for f in os.listdir(MODELS_DIR)
) if os.path.isdir(MODELS_DIR) else False


# ── Fixtures d'exemple ────────────────────────────────────────────────────────

POLE_FEATURES = {
    "grid_position": 1.0,
    "driver_age": 26.0,
    "dnf_rate_last10": 0.05,
    "driver_podiums_last5": 3.0,
    "circuit_history_avg": 2.5,
    "home_race": 0,
    "constructor_encoded": 0,
}

BACKMARKER_FEATURES = {
    "grid_position": 18.0,
    "driver_age": 32.0,
    "dnf_rate_last10": 0.30,
    "driver_podiums_last5": 0.0,
    "circuit_history_avg": 14.0,
    "home_race": 0,
    "constructor_encoded": 5,
}


# ── Tests pipeline complet ────────────────────────────────────────────────────

def test_build_dataset_no_nan():
    """Le dataset construit par build_dataset() ne doit pas avoir de NaN."""
    if not CSV_AVAILABLE:
        pytest.skip("CSV Kaggle manquants")
    from ml.preprocessing.pipeline import build_dataset
    X, y = build_dataset()
    assert X.isna().sum().sum() == 0, f"NaN détectés dans X : {X.isna().sum().sum()}"
    assert y.isna().sum() == 0, "NaN dans y"


def test_build_dataset_shape():
    """X doit avoir au moins 10 000 lignes et y doit être binaire."""
    if not CSV_AVAILABLE:
        pytest.skip("CSV Kaggle manquants")
    from ml.preprocessing.pipeline import build_dataset
    X, y = build_dataset()
    assert len(X) > 10_000, f"Trop peu de lignes : {len(X)}"
    assert set(y.unique()).issubset({0, 1}), "y n'est pas binaire"


def test_build_dataset_class_balance():
    """La classe 1 (podium) doit représenter 10-20% du dataset."""
    if not CSV_AVAILABLE:
        pytest.skip("CSV Kaggle manquants")
    from ml.preprocessing.pipeline import build_dataset
    X, y = build_dataset()
    ratio = y.mean()
    assert 0.10 < ratio < 0.20, f"Déséquilibre de classe anormal : {ratio:.2%}"


def test_automl_runs_without_error():
    """L'AutoML doit tourner sur un échantillon sans lever d'exception."""
    if not CSV_AVAILABLE:
        pytest.skip("CSV Kaggle manquants")
    import pandas as pd
    from ml.preprocessing.pipeline import build_dataset
    from ml.automl.automl import run_automl

    X, y = build_dataset()
    # Échantillon réduit pour accélérer le test
    X_sample = X.sample(min(2000, len(X)), random_state=42)
    y_sample = y.loc[X_sample.index]

    best_model, results = run_automl(X_sample, y_sample)
    assert best_model is not None
    assert len(results) == 5, "5 modèles attendus dans les résultats AutoML"
    for name, r in results.items():
        assert "f1_mean" in r
        assert r["f1_mean"] >= 0.0


# ── Tests prédiction ──────────────────────────────────────────────────────────

@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_predict_returns_required_keys():
    """predict() doit retourner podium_probability, podium_predicted, model_version."""
    from ml.predict import predict
    result = predict(POLE_FEATURES)
    assert "podium_probability" in result
    assert "podium_predicted" in result
    assert "model_version" in result


@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_predict_probability_range():
    """La probabilité de podium doit être dans [0, 1]."""
    from ml.predict import predict
    result = predict(POLE_FEATURES)
    assert 0.0 <= result["podium_probability"] <= 1.0


@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_predict_pole_higher_than_backmarker():
    """Un pilote en pole doit avoir une probabilité de podium > un pilote en fond de grille."""
    from ml.predict import predict
    pole_result = predict(POLE_FEATURES)
    back_result = predict(BACKMARKER_FEATURES)
    assert pole_result["podium_probability"] > back_result["podium_probability"], (
        f"Incohérence : pole={pole_result['podium_probability']:.3f} "
        f"≤ backmarker={back_result['podium_probability']:.3f}"
    )


@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_predict_list_versions():
    """list_versions() doit retourner au moins une version."""
    from ml.predict import list_versions
    versions = list_versions()
    assert len(versions) > 0, "Aucune version de modèle trouvée"


# ── Tests métriques ───────────────────────────────────────────────────────────

@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_metrics_file_exists():
    """latest_metrics.json doit exister après un entraînement."""
    from ml.evaluation.metrics import METRICS_PATH, load_metrics
    assert os.path.exists(METRICS_PATH), "latest_metrics.json manquant"
    metrics = load_metrics()
    assert "accuracy" in metrics
    assert "f1_score" in metrics


@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_metrics_accuracy_above_threshold():
    """L'accuracy du modèle doit dépasser 65%."""
    from ml.evaluation.metrics import load_metrics
    metrics = load_metrics()
    assert metrics.get("accuracy", 0) > 0.65, (
        f"Accuracy trop faible : {metrics.get('accuracy', 0):.2%}"
    )


@pytest.mark.skipif(not MODEL_AVAILABLE, reason="Aucun modèle entraîné")
def test_metrics_f1_above_threshold():
    """Le F1-score doit dépasser 0.40 (données déséquilibrées, ~15% de positifs)."""
    from ml.evaluation.metrics import load_metrics
    metrics = load_metrics()
    assert metrics.get("f1_score", 0) > 0.40, (
        f"F1-score trop faible : {metrics.get('f1_score', 0):.2%}"
    )


# ── Point d'entrée standalone ─────────────────────────────────────────────────

if __name__ == "__main__":
    print("=== Test d'inférence ML ===\n")

    if not MODEL_AVAILABLE:
        print("[INFO] Aucun modèle entraîné. Lancement de l'entraînement...")
        if not CSV_AVAILABLE:
            print("[SKIP] CSV Kaggle manquants. Impossible d'entraîner.")
            sys.exit(0)
        from ml.train import train
        result = train()
        print(f"[OK] Entraînement terminé : {result}")
    else:
        print(f"[OK] Modèle disponible dans {MODELS_DIR}")

    from ml.predict import predict, list_versions
    from ml.evaluation.metrics import load_metrics, print_report

    print("\n[Versions disponibles]")
    for v in list_versions():
        print(f"  - {v}")

    print("\n[Métriques du modèle actuel]")
    print_report()

    print("\n[Test prédiction — pilote en pole position]")
    r = predict(POLE_FEATURES)
    print(f"  podium_probability : {r['podium_probability']:.3f}")
    print(f"  podium_predicted   : {r['podium_predicted']}")
    print(f"  model_version      : {r['model_version']}")

    print("\n[Test prédiction — pilote en fond de grille]")
    r2 = predict(BACKMARKER_FEATURES)
    print(f"  podium_probability : {r2['podium_probability']:.3f}")
    print(f"  podium_predicted   : {r2['podium_predicted']}")

    assert r["podium_probability"] > r2["podium_probability"], \
        "ERREUR : Le pilote en pole devrait avoir une probabilité > backmarker"
    print("\n✓ Cohérence probabilités validée (pole > backmarker)")

    print("\n[Test intégration API→ML]")
    required_keys = {"podium_probability", "podium_predicted", "model_version"}
    assert required_keys.issubset(r.keys()), f"Clés manquantes : {required_keys - r.keys()}"
    print("✓ Toutes les clés requises par l'API sont présentes")
    print("\n✓ Tous les tests d'inférence passés !")
