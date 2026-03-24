import os
import sys
import uuid
from fastapi import APIRouter, Depends, BackgroundTasks, HTTPException
from sqlalchemy.orm import Session
from typing import List

# Ajout du chemin racine pour importer le module ml
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "../../../../"))

from app.db.database import get_db
from app.models.prediction_log import PredictionLog
from app.core.auth import get_current_admin

router = APIRouter()

# Stockage en mémoire du statut d'entraînement en cours
_training_jobs: dict = {}


@router.post("/train")
def train_model(
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db),
    current_admin=Depends(get_current_admin),
):
    """Lance l'entraînement du modèle ML en arrière-plan."""
    try:
        from ml.train import train as ml_train
    except ImportError:
        raise HTTPException(status_code=503, detail="Service ML non disponible")

    job_id = str(uuid.uuid4())[:8]
    _training_jobs[job_id] = {"status": "running", "result": None}

    def _run_training(jid: str):
        try:
            result = ml_train()
            _training_jobs[jid] = {"status": "done", "result": result}
        except Exception as e:
            _training_jobs[jid] = {"status": "error", "result": str(e)}

    background_tasks.add_task(_run_training, job_id)

    return {
        "status": "training_started",
        "job_id": job_id,
        "estimated_time_seconds": 120,
    }


@router.get("/train/{job_id}")
def get_training_status(job_id: str, current_admin=Depends(get_current_admin)):
    """Statut d'un entraînement en cours."""
    job = _training_jobs.get(job_id)
    if job is None:
        raise HTTPException(status_code=404, detail="Job introuvable")
    return {"job_id": job_id, **job}


@router.get("/eval")
def get_model_metrics(current_admin=Depends(get_current_admin)):
    """Métriques du modèle actuel (accuracy, F1, précision, rappel, matrice confusion)."""
    try:
        from ml.evaluation.metrics import load_metrics
    except ImportError:
        raise HTTPException(status_code=503, detail="Service ML non disponible")

    metrics = load_metrics()
    if not metrics:
        raise HTTPException(
            status_code=404,
            detail="Aucune métrique disponible. Lancez POST /admin/train d'abord.",
        )
    return metrics


@router.get("/model/versions")
def get_model_versions(current_admin=Depends(get_current_admin)):
    """Liste des versions de modèles sauvegardées."""
    try:
        from ml.predict import list_versions
    except ImportError:
        raise HTTPException(status_code=503, detail="Service ML non disponible")

    try:
        versions = list_versions()
    except FileNotFoundError:
        versions = []

    return {"versions": versions}


@router.post("/model/rollback/{version}")
def rollback_model(version: str, current_admin=Depends(get_current_admin)):
    """Charge une version précédente du modèle comme modèle actif."""
    try:
        from ml.predict import load_model_version
    except ImportError:
        raise HTTPException(status_code=503, detail="Service ML non disponible")

    try:
        load_model_version(version)
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))

    return {"status": "ok", "active_version": version}


@router.get("/stats")
def get_api_stats(
    db: Session = Depends(get_db),
    current_admin=Depends(get_current_admin),
):
    """Statistiques d'utilisation de l'API."""
    total_predictions = db.query(PredictionLog).count()

    # Prédictions des 7 derniers jours
    from sqlalchemy import func
    from datetime import datetime, timedelta
    week_ago = datetime.utcnow() - timedelta(days=7)
    recent_predictions = (
        db.query(PredictionLog)
        .filter(PredictionLog.created_at >= week_ago)
        .count()
    )

    # Taux de réussite : ratio podiums correctement prédits
    correct = db.query(PredictionLog).filter(
        PredictionLog.real_position.isnot(None),
        PredictionLog.predicted_position <= 3,
        PredictionLog.real_position <= 3,
    ).count()
    evaluated = db.query(PredictionLog).filter(
        PredictionLog.real_position.isnot(None)
    ).count()

    return {
        "total_predictions": total_predictions,
        "predictions_last_7_days": recent_predictions,
        "evaluated_predictions": evaluated,
        "correct_podium_predictions": correct,
        "accuracy_rate": round(correct / evaluated, 4) if evaluated > 0 else None,
    }
