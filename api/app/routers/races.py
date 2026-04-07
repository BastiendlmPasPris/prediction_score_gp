import os
import pandas as pd
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional

from app.db.database import get_db
from app.schemas.race import RaceResponse
from app.core.auth import get_current_user

router = APIRouter()

DATA_PATH = os.path.join(os.path.dirname(__file__), "../../../../ml/data/raw")

def _load_races_with_circuits() -> pd.DataFrame:
    races_path = os.path.join(DATA_PATH, "races.csv")
    circuits_path = os.path.join(DATA_PATH, "circuits.csv")
    if not os.path.exists(races_path) or not os.path.exists(circuits_path):
        raise HTTPException(status_code=503, detail="CSV introuvables.")
    races = pd.read_csv(races_path)
    circuits = pd.read_csv(circuits_path)[["circuitId", "name", "country"]]
    df = races.merge(circuits, on="circuitId", how="left", suffixes=("", "_circuit"))
    return df

def _row_to_race_response(row) -> RaceResponse:
    # Sécurité pour éviter les "nan" moches à l'affichage
    country = str(row.get("country", ""))
    if country.lower() == "nan": country = ""
    
    circuit = str(row.get("name_circuit", row.get("name", "")))
    if circuit.lower() == "nan": circuit = ""

    return RaceResponse(
        id=int(row["raceId"]),
        name=str(row["name"]),
        circuit=circuit,
        country=country,
        date=str(row["date"]),
        season=int(row["year"]),
        flag_url=None,
    )

@router.get("", response_model=List[RaceResponse])
def get_races(season: Optional[int] = Query(None), db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    df = _load_races_with_circuits()
    if season is not None:
        df = df[df["year"] == season]
    
    # On trie du premier au dernier Grand Prix
    df = df.sort_values(["year", "round"], ascending=[True, True])
    
    return [_row_to_race_response(row) for _, row in df.iterrows()]

@router.get("/{race_id}", response_model=RaceResponse)
def get_race(race_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    df = _load_races_with_circuits()
    row = df[df["raceId"] == race_id]
    if row.empty:
        raise HTTPException(status_code=404, detail="Course introuvable")
    return _row_to_race_response(row.iloc[0])