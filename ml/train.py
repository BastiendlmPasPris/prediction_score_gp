"""
Script d'entraînement — déclenché par POST /admin/train
"""
import joblib
import os
from datetime import datetime
from sklearn.model_selection import train_test_split

from ml.preprocessing.pipeline import build_dataset
from ml.automl.automl import run_automl
from ml.evaluation.metrics import compute_metrics, save_metrics

MODELS_DIR = os.path.join(os.path.dirname(__file__), "models/versions")


def train(test_size: float = 0.2) -> dict:
    """
    Pipeline complet d'entraînement :
    1. Preprocessing → dataset
    2. AutoML → meilleur modèle
    3. Évaluation → métriques
    4. Sauvegarde → model_vX.joblib
    """
    # 1. Construire le dataset
    X, y = build_dataset()

    # 2. Split train/test
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=test_size, random_state=42, stratify=y
    )

    # 3. AutoML
    best_model, automl_results = run_automl(X_train, y_train)

    # 4. Évaluation sur le test set
    y_pred = best_model.predict(X_test)
    metrics = compute_metrics(y_test, y_pred)
    print(f"[Train] Métriques test : {metrics}")

    # 5. Versionnage et sauvegarde
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    version = f"v_{timestamp}"
    model_path = os.path.join(MODELS_DIR, f"model_{version}.joblib")

    os.makedirs(MODELS_DIR, exist_ok=True)
    joblib.dump(best_model, model_path)
    print(f"[Train] Modèle sauvegardé : {model_path}")

    # 6. Sauvegarder les métriques
    save_metrics(metrics, version)

    return {"status": "success", "model_version": version, "metrics": metrics}


if __name__ == "__main__":
    result = train()
    print(result)
