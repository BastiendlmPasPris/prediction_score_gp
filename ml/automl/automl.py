"""
AutoML du Semestre 1 — Classification binaire (podium oui/non)
Teste plusieurs modèles classiques et sélectionne le meilleur.
"""
import pandas as pd
import numpy as np
from sklearn.model_selection import cross_val_score, StratifiedKFold
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.svm import SVC
from sklearn.neighbors import KNeighborsClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline


MODELS = {
    # class_weight="balanced" compense le déséquilibre ~85% non-podium / 15% podium
    "RandomForest": RandomForestClassifier(
        n_estimators=200,
        max_depth=10,
        min_samples_leaf=5,
        class_weight="balanced",
        random_state=42,
        n_jobs=-1,
    ),
    "GradientBoosting": GradientBoostingClassifier(
        n_estimators=150,
        max_depth=5,
        learning_rate=0.05,
        subsample=0.8,
        random_state=42,
    ),
    "LogisticRegression": Pipeline([
        ("scaler", StandardScaler()),
        ("clf", LogisticRegression(
            max_iter=1000,
            class_weight="balanced",
            C=0.5,
            random_state=42,
        )),
    ]),
    "KNN": Pipeline([
        ("scaler", StandardScaler()),
        ("clf", KNeighborsClassifier(n_neighbors=7, weights="distance")),
    ]),
    "SVM": Pipeline([
        ("scaler", StandardScaler()),
        ("clf", SVC(
            probability=True,
            class_weight="balanced",
            C=1.0,
            kernel="rbf",
            random_state=42,
        )),
    ]),
}


def run_automl(X: pd.DataFrame, y: pd.Series) -> tuple:
    """
    Lance l'AutoML : évalue tous les modèles et retourne le meilleur.
    Retourne : (best_model, results_dict)
    """
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    results = {}

    print("[AutoML] Évaluation des modèles...")
    for name, model in MODELS.items():
        scores = cross_val_score(model, X, y, cv=cv, scoring="f1", n_jobs=-1)
        results[name] = {
            "f1_mean": scores.mean(),
            "f1_std": scores.std(),
        }
        print(f"  {name}: F1 = {scores.mean():.4f} ± {scores.std():.4f}")

    # Sélection du meilleur modèle
    best_name = max(results, key=lambda k: results[k]["f1_mean"])
    best_model = MODELS[best_name]

    print(f"[AutoML] Meilleur modèle : {best_name} (F1 = {results[best_name]['f1_mean']:.4f})")

    # Entraînement final sur l'ensemble complet
    best_model.fit(X, y)

    return best_model, results
