from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List

from app.db.database import get_db
from app.schemas.prediction import PredictRequest, PredictionResponse
from app.core.auth import get_current_user

router = APIRouter()


@router.post("/predict", response_model=PredictionResponse)
def predict(request: PredictRequest, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """
    Prédiction pour un pilote sur un Grand Prix donné.
    Appelle le service ML (ml/predict.py) avec les features calculées.
    """
    # TODO: Construire le vecteur de features (grid_position, constructor_rank, etc.)
    # TODO: Appeler ml.predict.predict(features)
    # TODO: Sauvegarder la prédiction dans prediction_log
    # TODO: Retourner position prédite + probabilité podium
    pass


@router.post("/predict/race/{race_id}", response_model=List[PredictionResponse])
def predict_race(race_id: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Classement prédit complet pour les 20 pilotes d'un GP"""
    # TODO: Itérer sur les 20 pilotes engagés dans le GP
    # TODO: Appeler predict() pour chacun et trier par position prédite
    pass


@router.get("/predictions/history", response_model=List[PredictionResponse])
def get_history(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Historique des prédictions de l'utilisateur connecté"""
    # TODO: Récupérer les prédictions de l'utilisateur depuis prediction_log
    pass
