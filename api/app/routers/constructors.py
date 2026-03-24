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
def get_constructors(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Liste des écuries"""
    path = os.path.join(DATA_PATH, "constructors.csv")
    if not os.path.exists(path):
        raise HTTPException(
            status_code=503,
            detail="Données F1 non disponibles. Placez les CSV Kaggle dans ml/data/raw/",
        )
    df = pd.read_csv(path)
    return df[["constructorId", "name", "nationality"]].rename(
        columns={"constructorId": "id"}
    ).to_dict(orient="records")


@router.get("/{constructor_id}")
def get_constructor(
    constructor_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Détails et statistiques d'une écurie"""
    path = os.path.join(DATA_PATH, "constructors.csv")
    results_path = os.path.join(DATA_PATH, "results.csv")
    if not os.path.exists(path):
        raise HTTPException(status_code=503, detail="Données F1 non disponibles.")

    df = pd.read_csv(path)
    row = df[df["constructorId"] == constructor_id]
    if row.empty:
        raise HTTPException(status_code=404, detail="Écurie introuvable")

    result = row.iloc[0][["constructorId", "name", "nationality"]].to_dict()
    result["id"] = int(result.pop("constructorId"))

    if os.path.exists(results_path):
        res = pd.read_csv(results_path)
        cr = res[res["constructorId"] == constructor_id].copy()
        cr["positionOrder"] = pd.to_numeric(cr["positionOrder"], errors="coerce")
        result["wins"] = int((cr["positionOrder"] == 1).sum())
        result["podiums"] = int((cr["positionOrder"] <= 3).sum())

    return result
