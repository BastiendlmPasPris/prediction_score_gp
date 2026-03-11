from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from app.db.database import get_db
from app.schemas.race import RaceResponse
from app.core.auth import get_current_user

router = APIRouter()


@router.get("", response_model=List[RaceResponse])
def get_races(
    season: Optional[int] = Query(None, description="Filtrer par saison"),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    """Liste des Grands Prix, filtrable par saison"""
    # TODO: Requête en BDD avec filtre optionnel sur la saison
    pass


@router.get("/{race_id}", response_model=RaceResponse)
def get_race(race_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Détails d'un Grand Prix"""
    # TODO: Récupérer la course par son ID, 404 si non trouvée
    pass
