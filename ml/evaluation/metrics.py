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


def print_report(metrics: dict = None):
    """Affiche un bilan lisible des métriques en console."""
    if metrics is None:
        metrics = load_metrics()
    if not metrics:
        print("[Metrics] Aucune métrique disponible. Lancez d'abord ml/train.py")
        return
    print("=" * 42)
    print(f"  Modèle    : {metrics.get('model_version', 'N/A')}")
    print(f"  Accuracy  : {metrics.get('accuracy', 0):.2%}")
    print(f"  F1-Score  : {metrics.get('f1_score', 0):.2%}")
    print(f"  Precision : {metrics.get('precision', 0):.2%}")
    print(f"  Recall    : {metrics.get('recall', 0):.2%}")
    cm = metrics.get("confusion_matrix")
    if cm:
        print(f"  Confusion : TN={cm[0][0]}  FP={cm[0][1]}")
        print(f"              FN={cm[1][0]}  TP={cm[1][1]}")
    print("=" * 42)
