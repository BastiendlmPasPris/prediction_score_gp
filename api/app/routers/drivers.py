import os
import pandas as pd
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app.db.database import get_db
from app.schemas.driver import DriverResponse, DriverStatsResponse
from app.core.auth import get_current_user

router = APIRouter()

DATA_PATH = os.path.join(os.path.dirname(__file__), "../../../../ml/data/raw")


def _check_csv(*names: str):
    for name in names:
        if not os.path.exists(os.path.join(DATA_PATH, f"{name}.csv")):
            raise HTTPException(
                status_code=503,
                detail="Données F1 non disponibles. Placez les CSV Kaggle dans ml/data/raw/",
            )


def _get_driver_team(driver_id: int, results: pd.DataFrame, constructors: pd.DataFrame) -> str:
    """Retourne l'écurie la plus récente du pilote."""
    dr = results[results["driverId"] == driver_id].sort_values("raceId", ascending=False)
    if dr.empty:
        return "Unknown"
    constructor_id = dr.iloc[0]["constructorId"]
    row = constructors[constructors["constructorId"] == constructor_id]
    return str(row.iloc[0]["name"]) if not row.empty else "Unknown"


@router.get("", response_model=List[DriverResponse])
def get_drivers(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Liste de tous les pilotes"""
    _check_csv("drivers", "results", "constructors")
    drivers = pd.read_csv(os.path.join(DATA_PATH, "drivers.csv"))
    results = pd.read_csv(os.path.join(DATA_PATH, "results.csv"))
    constructors = pd.read_csv(os.path.join(DATA_PATH, "constructors.csv"))

    out = []
    for _, row in drivers.iterrows():
        out.append(DriverResponse(
            id=int(row["driverId"]),
            first_name=str(row["forename"]),
            last_name=str(row["surname"]),
            nationality=str(row["nationality"]),
            team=_get_driver_team(int(row["driverId"]), results, constructors),
            photo_url=None,
        ))
    return out


@router.get("/{driver_id}", response_model=DriverResponse)
def get_driver(driver_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Fiche détaillée d'un pilote"""
    _check_csv("drivers", "results", "constructors")
    drivers = pd.read_csv(os.path.join(DATA_PATH, "drivers.csv"))
    results = pd.read_csv(os.path.join(DATA_PATH, "results.csv"))
    constructors = pd.read_csv(os.path.join(DATA_PATH, "constructors.csv"))

    row = drivers[drivers["driverId"] == driver_id]
    if row.empty:
        raise HTTPException(status_code=404, detail="Pilote introuvable")
    r = row.iloc[0]
    return DriverResponse(
        id=int(r["driverId"]),
        first_name=str(r["forename"]),
        last_name=str(r["surname"]),
        nationality=str(r["nationality"]),
        team=_get_driver_team(driver_id, results, constructors),
        photo_url=None,
    )


@router.get("/{driver_id}/stats", response_model=DriverStatsResponse)
def get_driver_stats(driver_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Statistiques d'un pilote (victoires, podiums, poles, historique circuits)"""
    _check_csv("results", "qualifying", "circuits")
    results = pd.read_csv(os.path.join(DATA_PATH, "results.csv"))
    qualifying = pd.read_csv(os.path.join(DATA_PATH, "qualifying.csv"))
    circuits = pd.read_csv(os.path.join(DATA_PATH, "circuits.csv"))[["circuitId", "name"]]

    dr = results[results["driverId"] == driver_id].copy()
    if dr.empty:
        raise HTTPException(status_code=404, detail="Pilote introuvable")

    dr["positionOrder"] = pd.to_numeric(dr["positionOrder"], errors="coerce")
    wins = int((dr["positionOrder"] == 1).sum())
    podiums = int((dr["positionOrder"] <= 3).sum())

    # Poles depuis qualifying
    dq = qualifying[qualifying["driverId"] == driver_id]
    poles = int((pd.to_numeric(dq["position"], errors="coerce") == 1).sum())

    # 10 dernières positions de course
    last10 = dr.sort_values("raceId", ascending=False).head(10)["positionOrder"].dropna().astype(int).tolist()

    # Performances par circuit : position moyenne
    races_df = pd.read_csv(os.path.join(DATA_PATH, "races.csv"))[["raceId", "circuitId"]]
    merged = dr.merge(races_df, on="raceId", how="left").merge(circuits, on="circuitId", how="left")
    circuit_perf = (
        merged.groupby("name")["positionOrder"]
        .mean()
        .round(2)
        .to_dict()
    )

    return DriverStatsResponse(
        driver_id=driver_id,
        wins=wins,
        podiums=podiums,
        poles=poles,
        last_10_results=last10,
        circuit_performances=circuit_perf,
    )
