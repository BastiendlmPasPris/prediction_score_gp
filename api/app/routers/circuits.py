import os
import pandas as pd
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app.db.database import get_db
from app.core.auth import get_current_user

router = APIRouter()

DATA_PATH = os.path.join(os.path.dirname(__file__), "../../../../ml/data/raw")


@router.get("")
def get_circuits(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Liste des circuits"""
    path = os.path.join(DATA_PATH, "circuits.csv")
    if not os.path.exists(path):
        raise HTTPException(
            status_code=503,
            detail="Données F1 non disponibles. Placez les CSV Kaggle dans ml/data/raw/",
        )
    df = pd.read_csv(path)
    return df[["circuitId", "name", "location", "country", "lat", "lng"]].rename(
        columns={"circuitId": "id"}
    ).to_dict(orient="records")


@router.get("/{circuit_id}")
def get_circuit(
    circuit_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Détails d'un circuit"""
    path = os.path.join(DATA_PATH, "circuits.csv")
    if not os.path.exists(path):
        raise HTTPException(status_code=503, detail="Données F1 non disponibles.")
    df = pd.read_csv(path)
    row = df[df["circuitId"] == circuit_id]
    if row.empty:
        raise HTTPException(status_code=404, detail="Circuit introuvable")
    result = row.iloc[0][["circuitId", "name", "location", "country", "lat", "lng"]].to_dict()
    result["id"] = int(result.pop("circuitId"))
    return result
