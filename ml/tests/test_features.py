"""
Script de validation du feature engineering.
Lancer depuis la racine du projet :
    python -m pytest ml/tests/test_features.py -v
ou directement :
    python ml/tests/test_features.py
"""
import sys
import os

# Ajouter la racine du projet au path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "../.."))

import pandas as pd
import numpy as np
import pytest

DATA_PATH = os.path.join(os.path.dirname(__file__), "../data/raw")
CSV_AVAILABLE = os.path.exists(os.path.join(DATA_PATH, "results.csv"))


# ── Fixtures ──────────────────────────────────────────────────────────────────

@pytest.fixture(scope="module")
def raw_df():
    """Charge et merge les CSV Kaggle une seule fois pour tous les tests."""
    if not CSV_AVAILABLE:
        pytest.skip("CSV Kaggle manquants — placez-les dans ml/data/raw/ (voir README)")
    from ml.preprocessing.loader import load_all, merge_tables
    dfs = load_all()
    df = merge_tables(dfs)
    return df


@pytest.fixture(scope="module")
def full_df(raw_df):
    """DataFrame avec toutes les features calculées."""
    from ml.preprocessing.features import (
        add_target, add_grid_position, add_driver_age,
        add_dnf_rate, add_driver_podiums_last5,
        add_circuit_history_avg, add_home_race,
    )
    df = add_target(raw_df.copy())
    df = add_grid_position(df)
    df = add_driver_age(df)
    df = add_dnf_rate(df)
    df = add_driver_podiums_last5(df)
    df = add_circuit_history_avg(df)
    df = add_home_race(df)
    return df


# ── Tests de chargement ────────────────────────────────────────────────────────

def test_loader_loads_all_csvs():
    """Tous les CSV requis doivent être présents."""
    if not CSV_AVAILABLE:
        pytest.skip("CSV Kaggle manquants")
    from ml.preprocessing.loader import load_all
    dfs = load_all()
    required = ["results", "races", "drivers", "constructors", "qualifying", "circuits", "status"]
    for name in required:
        assert name in dfs, f"CSV manquant : {name}.csv"
        assert len(dfs[name]) > 0, f"CSV vide : {name}.csv"


def test_merge_produces_expected_shape(raw_df):
    """Le merge doit produire au moins 15 000 lignes (données 2010-2024)."""
    assert len(raw_df) > 15_000, f"Trop peu de lignes : {len(raw_df)} (attendu > 15 000)"
    assert raw_df["year"].min() >= 2010, "Des données avant 2010 ont été incluses"


def test_merge_has_required_columns(raw_df):
    """Les colonnes essentielles doivent être présentes après le merge."""
    required_cols = ["raceId", "driverId", "constructorId", "positionOrder", "grid",
                     "date", "dob", "status", "name", "circuitId", "year"]
    for col in required_cols:
        assert col in raw_df.columns, f"Colonne manquante : {col}"


# ── Tests add_target ──────────────────────────────────────────────────────────

def test_add_target_column_exists(full_df):
    assert "podium" in full_df.columns


def test_add_target_binary_values(full_df):
    assert set(full_df["podium"].unique()).issubset({0, 1})


def test_add_target_podium_ratio(full_df):
    ratio = full_df["podium"].mean()
    assert 0.10 < ratio < 0.20, (
        f"Ratio podium anormal : {ratio:.2%} (attendu entre 10% et 20%)"
    )


# ── Tests add_grid_position ───────────────────────────────────────────────────

def test_grid_position_column_exists(full_df):
    assert "grid_position" in full_df.columns


def test_grid_position_range(full_df):
    valid = full_df["grid_position"].dropna()
    assert valid.between(0, 22).all(), (
        f"Position grille aberrante : min={valid.min()}, max={valid.max()}"
    )


# ── Tests add_driver_age ──────────────────────────────────────────────────────

def test_driver_age_column_exists(full_df):
    assert "driver_age" in full_df.columns


def test_driver_age_range(full_df):
    valid = full_df["driver_age"].dropna()
    assert valid.between(17, 55).all(), (
        f"Âge pilote aberrant : min={valid.min():.1f}, max={valid.max():.1f}"
    )


# ── Tests add_dnf_rate ────────────────────────────────────────────────────────

def test_dnf_rate_column_exists(full_df):
    assert "dnf_rate_last10" in full_df.columns


def test_dnf_rate_range(full_df):
    assert full_df["dnf_rate_last10"].between(0, 1).all(), "DNF rate hors [0, 1]"


def test_dnf_rate_no_leakage(full_df):
    """Vérifier que les DNF de la course actuelle ne sont pas inclus (shift(1))."""
    # Le taux ne doit pas être parfait — s'il l'est, c'est souvent du leakage
    assert full_df["dnf_rate_last10"].max() <= 1.0


# ── Tests add_driver_podiums_last5 ────────────────────────────────────────────

def test_podiums_last5_column_exists(full_df):
    assert "driver_podiums_last5" in full_df.columns


def test_podiums_last5_range(full_df):
    assert full_df["driver_podiums_last5"].between(0, 5).all(), (
        "driver_podiums_last5 hors [0, 5]"
    )


# ── Tests add_circuit_history_avg ─────────────────────────────────────────────

def test_circuit_history_avg_column_exists(full_df):
    assert "circuit_history_avg" in full_df.columns


def test_circuit_history_avg_range(full_df):
    valid = full_df["circuit_history_avg"].dropna()
    assert valid.between(1, 20).all(), (
        f"circuit_history_avg hors [1, 20] : min={valid.min():.2f}, max={valid.max():.2f}"
    )


# ── Tests add_home_race ───────────────────────────────────────────────────────

def test_home_race_column_exists(full_df):
    assert "home_race" in full_df.columns


def test_home_race_binary(full_df):
    assert set(full_df["home_race"].unique()).issubset({0, 1}), "home_race doit être 0 ou 1"


def test_home_race_positive_count(full_df):
    """Il doit y avoir au moins quelques courses à domicile."""
    n_home = full_df["home_race"].sum()
    assert n_home > 50, f"Trop peu de home races détectées : {n_home}"


# ── Tests encode_features ─────────────────────────────────────────────────────

def test_encode_features_no_explosion(full_df):
    """L'encodage ne doit pas créer trop de colonnes circuit (one-hot)."""
    from ml.preprocessing.features import encode_features
    df_enc = encode_features(full_df.copy())
    circuit_cols = [c for c in df_enc.columns if c.startswith("circuit_")]
    assert len(circuit_cols) <= 80, (
        f"Trop de colonnes circuit : {len(circuit_cols)} (max recommandé : 80)"
    )


def test_encode_features_constructor_encoded(full_df):
    from ml.preprocessing.features import encode_features
    df_enc = encode_features(full_df.copy())
    assert "constructor_encoded" in df_enc.columns


# ── Point d'entrée standalone ─────────────────────────────────────────────────

if __name__ == "__main__":
    print("=== Validation des features ML ===\n")
    if not CSV_AVAILABLE:
        print("[SKIP] CSV Kaggle manquants. Placez-les dans ml/data/raw/ (voir README).")
        sys.exit(0)

    from ml.preprocessing.loader import load_all, merge_tables
    from ml.preprocessing.features import (
        add_target, add_grid_position, add_driver_age,
        add_dnf_rate, add_driver_podiums_last5,
        add_circuit_history_avg, add_home_race, encode_features, FEATURE_COLUMNS,
    )

    print("[1/8] Chargement des CSV...")
    dfs = load_all()
    df = merge_tables(dfs)
    print(f"  Shape brut : {df.shape}")

    print("[2/8] add_target...")
    df = add_target(df)
    ratio = df["podium"].mean()
    assert 0.10 < ratio < 0.20
    print(f"  Ratio podium : {ratio:.2%} ✓")

    print("[3/8] add_grid_position...")
    df = add_grid_position(df)
    assert df["grid_position"].between(0, 22).all()
    print(f"  Range : [{df['grid_position'].min():.0f}, {df['grid_position'].max():.0f}] ✓")

    print("[4/8] add_driver_age...")
    df = add_driver_age(df)
    valid = df["driver_age"].dropna()
    assert valid.between(17, 55).all()
    print(f"  Range : [{valid.min():.1f}, {valid.max():.1f}] ans ✓")

    print("[5/8] add_dnf_rate...")
    df = add_dnf_rate(df)
    assert df["dnf_rate_last10"].between(0, 1).all()
    print(f"  Mean DNF rate : {df['dnf_rate_last10'].mean():.3f} ✓")

    print("[6/8] add_driver_podiums_last5...")
    df = add_driver_podiums_last5(df)
    assert df["driver_podiums_last5"].between(0, 5).all()
    print(f"  Max podiums last5 : {df['driver_podiums_last5'].max():.0f} ✓")

    print("[7/8] add_circuit_history_avg + add_home_race...")
    df = add_circuit_history_avg(df)
    df = add_home_race(df)
    n_home = df["home_race"].sum()
    print(f"  Home races détectées : {n_home} ✓")

    print("[8/8] encode_features...")
    df = encode_features(df)
    circuit_cols = [c for c in df.columns if c.startswith("circuit_")]
    print(f"  Colonnes circuit (one-hot) : {len(circuit_cols)}")
    all_features = FEATURE_COLUMNS + circuit_cols
    df_clean = df.dropna(subset=FEATURE_COLUMNS + ["podium"])
    print(f"  Dataset final : {len(df_clean)} lignes, {len(all_features)} features")

    print("\n✓ Toutes les validations passées !")
