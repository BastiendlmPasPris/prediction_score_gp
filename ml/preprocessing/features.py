import pandas as pd
import numpy as np


def add_target(df: pd.DataFrame) -> pd.DataFrame:
    """Variable cible : podium (1 si position <= 3, sinon 0)"""
    df["podium"] = (df["positionOrder"] <= 3).astype(int)
    return df


def add_grid_position(df: pd.DataFrame) -> pd.DataFrame:
    """Position sur la grille de départ (depuis qualifying)"""
    df["grid_position"] = pd.to_numeric(df["grid"], errors="coerce").fillna(20)
    return df


def add_driver_age(df: pd.DataFrame) -> pd.DataFrame:
    """Âge du pilote au moment de la course"""
    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df["dob"] = pd.to_datetime(df["dob"], errors="coerce")
    df["driver_age"] = ((df["date"] - df["dob"]).dt.days / 365.25).round(1)
    return df


def add_dnf_rate(df: pd.DataFrame, window: int = 10) -> pd.DataFrame:
    """Taux d'abandon sur les N dernières courses"""
    df = df.sort_values(["driverId", "raceId"])
    df["is_dnf"] = (~df["status"].str.contains("Finished|\\+", na=False)).astype(int)
    df["dnf_rate_last10"] = (
        df.groupby("driverId")["is_dnf"]
        .transform(lambda x: x.shift(1).rolling(window, min_periods=1).mean())
    ).fillna(0)
    return df


def add_driver_podiums_last5(df: pd.DataFrame) -> pd.DataFrame:
    """Nombre de podiums sur les 5 dernières courses"""
    df = df.sort_values(["driverId", "raceId"])
    df["driver_podiums_last5"] = (
        df.groupby("driverId")["podium"]
        .transform(lambda x: x.shift(1).rolling(5, min_periods=1).sum())
    ).fillna(0)
    return df


def add_circuit_history_avg(df: pd.DataFrame) -> pd.DataFrame:
    """Position moyenne du pilote sur ce circuit (historique)"""
    df = df.sort_values(["driverId", "raceId"])
    df["circuit_history_avg"] = (
        df.groupby(["driverId", "circuitId"])["positionOrder"]
        .transform(lambda x: x.shift(1).expanding().mean())
    ).fillna(10)
    return df


def add_home_race(df: pd.DataFrame) -> pd.DataFrame:
    """Le pilote court-il dans son pays natal ? (0 ou 1)"""
    # Correspondance entre la nationalité Kaggle et le pays du circuit
    nationality_to_country = {
        "British": "UK",
        "German": "Germany",
        "Spanish": "Spain",
        "Finnish": "Finland",
        "French": "France",
        "Italian": "Italy",
        "Australian": "Australia",
        "Dutch": "Netherlands",
        "Mexican": "Mexico",
        "Canadian": "Canada",
        "Brazilian": "Brazil",
        "Japanese": "Japan",
        "American": "USA",
        "Austrian": "Austria",
        "Belgian": "Belgium",
        "Danish": "Denmark",
        "Thai": "Thailand",
        "Monegasque": "Monaco",
        "New Zealander": "New Zealand",
        "Swiss": "Switzerland",
        "Swedish": "Sweden",
        "Colombian": "Colombia",
        "Argentinian": "Argentina",
        "Hungarian": "Hungary",
        "Polish": "Poland",
        "Russian": "Russia",
        "Chinese": "China",
        "Indian": "India",
        "Singaporean": "Singapore",
    }

    # Charger circuits.csv si pas déjà dans le DataFrame
    if "country" not in df.columns:
        import os
        circuits_path = os.path.join(os.path.dirname(__file__), "../data/raw/circuits.csv")
        if os.path.exists(circuits_path):
            circuits = pd.read_csv(circuits_path)[["circuitId", "country"]]
            df = df.merge(circuits, on="circuitId", how="left")
        else:
            df["home_race"] = 0
            return df

    df["driver_country"] = df["nationality"].map(nationality_to_country)
    df["home_race"] = (df["driver_country"] == df["country"]).astype(int)
    df = df.drop(columns=["driver_country"], errors="ignore")
    return df


def encode_features(df: pd.DataFrame) -> pd.DataFrame:
    """Encodage des variables catégorielles + normalisation"""
    from sklearn.preprocessing import LabelEncoder
    le = LabelEncoder()
    df["constructor_encoded"] = le.fit_transform(df["name_constructor"].fillna("Unknown"))
    # One-hot encoding des circuits (top 30 circuits)
    circuit_dummies = pd.get_dummies(df["circuitId"], prefix="circuit")
    df = pd.concat([df, circuit_dummies], axis=1)
    return df


FEATURE_COLUMNS = [
    "grid_position",
    "driver_age",
    "dnf_rate_last10",
    "driver_podiums_last5",
    "circuit_history_avg",
    "home_race",
    "constructor_encoded",
]
