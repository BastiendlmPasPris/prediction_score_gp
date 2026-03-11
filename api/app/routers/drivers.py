from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List

from app.db.database import get_db
from app.schemas.driver import DriverResponse, DriverStatsResponse
from app.core.auth import get_current_user

router = APIRouter()


@router.get("", response_model=List[DriverResponse])
def get_drivers(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Liste de tous les pilotes"""
    # TODO: Retourner la liste des pilotes depuis la BDD
    pass


@router.get("/{driver_id}", response_model=DriverResponse)
def get_driver(driver_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Fiche détaillée d'un pilote"""
    # TODO: Récupérer le pilote par son ID
    pass


@router.get("/{driver_id}/stats", response_model=DriverStatsResponse)
def get_driver_stats(driver_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Statistiques d'un pilote (victoires, podiums, poles, historique circuits)"""
    # TODO: Calculer et retourner les stats à partir des données CSV/BDD
    pass
