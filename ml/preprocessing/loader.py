import pandas as pd
import os

DATA_PATH = os.path.join(os.path.dirname(__file__), "../data/raw")


def load_all() -> dict:
    """Charge tous les fichiers CSV Kaggle et retourne un dictionnaire de DataFrames"""
    files = [
        "results", "races", "drivers", "constructors", "qualifying",
        "circuits", "driver_standings", "constructor_standings",
        "constructor_results", "status"
    ]
    dfs = {}
    for f in files:
        path = os.path.join(DATA_PATH, f"{f}.csv")
        if os.path.exists(path):
            dfs[f] = pd.read_csv(path)
        else:
            print(f"[WARN] Fichier manquant : {f}.csv")
    return dfs


def merge_tables(dfs: dict) -> pd.DataFrame:
    """
    Effectue les jointures principales pour créer la table de travail ML.
    Table centrale : results.csv
    """
    df = dfs["results"].copy()
    # Jointure avec races (date, saison, circuit)
    df = df.merge(dfs["races"][["raceId", "year", "circuitId", "date", "name"]], on="raceId", how="left")
    # Jointure avec drivers (nom, nationalité, date de naissance)
    df = df.merge(dfs["drivers"][["driverId", "forename", "surname", "nationality", "dob"]], on="driverId", how="left")
    # Jointure avec constructors (nom écurie)
    df = df.merge(dfs["constructors"][["constructorId", "name"]], on="constructorId", how="left", suffixes=("", "_constructor"))
    # Jointure avec qualifying (position grille)
    qualifying = dfs["qualifying"][["raceId", "driverId", "position"]].rename(columns={"position": "grid_position_q"})
    df = df.merge(qualifying, on=["raceId", "driverId"], how="left")
    # Jointure avec status (type d'abandon)
    df = df.merge(dfs["status"][["statusId", "status"]], on="statusId", how="left")

    # Filtrer les données modernes (>= 2010)
    df = df[df["year"] >= 2010].copy()
    df = df.sort_values(["year", "raceId", "driverId"]).reset_index(drop=True)

    return df
