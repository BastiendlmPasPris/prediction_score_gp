from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List

from app.db.database import get_db
from app.core.auth import get_current_admin

router = APIRouter()


@router.post("/train")
def train_model(db: Session = Depends(get_db), current_admin=Depends(get_current_admin)):
    """Lance l'entraînement du modèle ML (tâche asynchrone)"""
    # TODO: Appeler ml/train.py en arrière-plan (BackgroundTasks)
    # TODO: Retourner job_id + estimated_time
    pass


@router.get("/eval")
def get_model_metrics(current_admin=Depends(get_current_admin)):
    """Métriques du modèle actuel (accuracy, F1, précision, rappel, matrice confusion)"""
    # TODO: Lire les métriques depuis ml/evaluation/
    pass


@router.get("/model/versions")
def get_model_versions(current_admin=Depends(get_current_admin)):
    """Liste des versions de modèles sauvegardées"""
    # TODO: Lister les fichiers .joblib dans ml/models/versions/
    pass


@router.post("/model/rollback/{version}")
def rollback_model(version: str, current_admin=Depends(get_current_admin)):
    """Revenir à une version précédente du modèle"""
    # TODO: Copier le fichier model_v{version}.joblib comme modèle actif
    pass


@router.get("/stats")
def get_api_stats(current_admin=Depends(get_current_admin)):
    """Statistiques d'utilisation de l'API (nb requêtes, temps de réponse)"""
    # TODO: Lire les logs depuis la BDD ou fichier de log
    pass
