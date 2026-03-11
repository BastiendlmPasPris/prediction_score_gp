import json
import os
import numpy as np
from sklearn.metrics import (
    accuracy_score, f1_score, precision_score,
    recall_score, confusion_matrix
)

METRICS_PATH = os.path.join(os.path.dirname(__file__), "latest_metrics.json")


def compute_metrics(y_true, y_pred) -> dict:
    """Calcule et retourne toutes les métriques d'évaluation"""
    cm = confusion_matrix(y_true, y_pred)
    metrics = {
        "accuracy":  round(accuracy_score(y_true, y_pred), 4),
        "f1_score":  round(f1_score(y_true, y_pred), 4),
        "precision": round(precision_score(y_true, y_pred), 4),
        "recall":    round(recall_score(y_true, y_pred), 4),
        "confusion_matrix": cm.tolist()
    }
    return metrics


def save_metrics(metrics: dict, model_version: str):
    """Sauvegarde les métriques dans un fichier JSON"""
    metrics["model_version"] = model_version
    with open(METRICS_PATH, "w") as f:
        json.dump(metrics, f, indent=2)
    print(f"[Metrics] Sauvegardées dans {METRICS_PATH}")


def load_metrics() -> dict:
    """Charge les métriques du dernier modèle entraîné"""
    if os.path.exists(METRICS_PATH):
        with open(METRICS_PATH, "r") as f:
            return json.load(f)
    return {}
