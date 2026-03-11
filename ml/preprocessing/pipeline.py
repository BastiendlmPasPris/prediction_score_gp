import pandas as pd
from ml.preprocessing.loader import load_all, merge_tables
from ml.preprocessing.features import (
    add_target, add_grid_position, add_driver_age,
    add_dnf_rate, add_driver_podiums_last5,
    add_circuit_history_avg, add_home_race,
    encode_features, FEATURE_COLUMNS
)


def build_dataset() -> tuple:
    """
    Pipeline complet : CSV bruts → (X, y) prêts pour l'AutoML
    Retourne : (X: DataFrame, y: Series)
    """
    print("[Pipeline] Chargement des CSV...")
    dfs = load_all()

    print("[Pipeline] Jointures des tables...")
    df = merge_tables(dfs)

    print("[Pipeline] Calcul des features...")
    df = add_target(df)
    df = add_grid_position(df)
    df = add_driver_age(df)
    df = add_dnf_rate(df)
    df = add_driver_podiums_last5(df)
    df = add_circuit_history_avg(df)
    df = add_home_race(df)
    df = encode_features(df)

    # Supprimer les lignes avec valeurs manquantes sur les features clés
    df = df.dropna(subset=FEATURE_COLUMNS + ["podium"])

    # Sélection des colonnes circuit (one-hot)
    circuit_cols = [c for c in df.columns if c.startswith("circuit_")]
    all_features = FEATURE_COLUMNS + circuit_cols

    X = df[all_features]
    y = df["podium"]

    print(f"[Pipeline] Dataset prêt : {len(X)} lignes, {len(all_features)} features")
    return X, y
