import os
import sys
import pandas as pd
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

# Ajout du chemin racine pour importer le module ml
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "../../../../"))

from app.db.database import get_db
from app.models.prediction_log import PredictionLog
from app.schemas.prediction import PredictRequest, PredictionResponse
from app.core.auth import get_current_user

router = APIRouter()

DATA_PATH = os.path.join(os.path.dirname(__file__), "../../../../ml/data/raw")


# ── Helpers ──────────────────────────────────────────────────────────────────

def _load_csv(name: str) -> pd.DataFrame:
    path = os.path.join(DATA_PATH, f"{name}.csv")
    if not os.path.exists(path):
        return pd.DataFrame()
    return pd.read_csv(path)


def _get_driver_name(driver_id: int) -> str:
    df = _load_csv("drivers")
    if df.empty:
        return f"Driver #{driver_id}"
    row = df[df["driverId"] == driver_id]
    if row.empty:
        return f"Driver #{driver_id}"
    r = row.iloc[0]
    return f"{r['forename']} {r['surname']}"


def _get_race_name(race_id: int) -> str:
    df = _load_csv("races")
    if df.empty:
        return f"Race #{race_id}"
    row = df[df["raceId"] == race_id]
    return str(row.iloc[0]["name"]) if not row.empty else f"Race #{race_id}"


def _build_features(race_id: int, driver_id: int) -> dict:
    """
    Construit le vecteur de features pour l'inférence ML.
    Utilise les CSV si disponibles, sinon utilise des valeurs par défaut.
    """
    features = {
        "grid_position": 10.0,
        "driver_age": 28.0,
        "dnf_rate_last10": 0.1,
        "driver_podiums_last5": 1.0,
        "circuit_history_avg": 8.0,
        "home_race": 0,
        "constructor_encoded": 0,
    }

    results_df = _load_csv("results")
    drivers_df = _load_csv("drivers")
    qualifying_df = _load_csv("qualifying")
    races_df = _load_csv("races")

    # Position de départ (grille)
    if not qualifying_df.empty:
        q = qualifying_df[
            (qualifying_df["raceId"] == race_id) & (qualifying_df["driverId"] == driver_id)
        ]
        if not q.empty:
            pos = pd.to_numeric(q.iloc[0]["position"], errors="coerce")
            if pd.notna(pos):
                features["grid_position"] = float(pos)

    # Âge du pilote
    if not drivers_df.empty and not races_df.empty:
        dr = drivers_df[drivers_df["driverId"] == driver_id]
        rc = races_df[races_df["raceId"] == race_id]
        if not dr.empty and not rc.empty:
            dob = pd.to_datetime(dr.iloc[0]["dob"], errors="coerce")
            race_date = pd.to_datetime(rc.iloc[0]["date"], errors="coerce")
            if pd.notna(dob) and pd.notna(race_date):
                features["driver_age"] = round((race_date - dob).days / 365.25, 1)

    # Statistiques historiques depuis results
    if not results_df.empty:
        dr_results = results_df[
            (results_df["driverId"] == driver_id) & (results_df["raceId"] < race_id)
        ].sort_values("raceId", ascending=False)

        if len(dr_results) >= 3:
            # DNF rate sur les 10 dernières courses
            status_df = _load_csv("status")
            if not status_df.empty:
                dr_with_status = dr_results.head(10).merge(
                    status_df[["statusId", "status"]], on="statusId", how="left"
                )
                dnf = (~dr_with_status["status"].str.contains(
                    r"Finished|\+", na=False, regex=True
                )).mean()
                features["dnf_rate_last10"] = round(float(dnf), 3)

            # Podiums sur les 5 dernières courses
            last5_pos = pd.to_numeric(
                dr_results.head(5)["positionOrder"], errors="coerce"
            )
            features["driver_podiums_last5"] = float((last5_pos <= 3).sum())

            # Position moyenne sur ce circuit
            if not races_df.empty:
                circuit_id = races_df[races_df["raceId"] == race_id]
                if not circuit_id.empty:
                    cid = circuit_id.iloc[0]["circuitId"]
                    same_circuit_races = races_df[races_df["circuitId"] == cid]["raceId"]
                    circuit_results = dr_results[
                        dr_results["raceId"].isin(same_circuit_races)
                    ]
                    if not circuit_results.empty:
                        avg = pd.to_numeric(
                            circuit_results["positionOrder"], errors="coerce"
                        ).mean()
                        if pd.notna(avg):
                            features["circuit_history_avg"] = round(float(avg), 2)

    return features


def _proba_to_position(proba: float) -> int:
    """Estime la position finale à partir de la probabilité de podium (modèle binaire)."""
    if proba >= 0.70:
        return 1
    if proba >= 0.50:
        return 2
    if proba >= 0.35:
        return 3
    # Position estimée linéairement pour les non-podiums (4 à 20)
    return min(20, max(4, round(4 + (1 - proba) * 16)))


# ── Endpoints ─────────────────────────────────────────────────────────────────

@router.post("/predict", response_model=PredictionResponse)
def predict_driver(
    request: PredictRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Prédiction pour un pilote sur un Grand Prix donné."""
    try:
        from ml.predict import predict as ml_predict
    except ImportError:
        raise HTTPException(status_code=503, detail="Service ML non disponible")

    features = _build_features(request.race_id, request.driver_id)

    try:
        ml_result = ml_predict(features)
    except FileNotFoundError:
        raise HTTPException(
            status_code=503,
            detail="Aucun modèle entraîné. Lancez POST /admin/train d'abord.",
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur ML : {str(e)}")

    predicted_pos = _proba_to_position(ml_result["podium_probability"])

    # Sauvegarde dans prediction_log
    log = PredictionLog(
        user_id=current_user.id,
        race_id=request.race_id,
        driver_id=request.driver_id,
        predicted_position=predicted_pos,
        podium_probability=ml_result["podium_probability"],
        model_version=ml_result["model_version"],
    )
    db.add(log)
    db.commit()

    return PredictionResponse(
        driver=_get_driver_name(request.driver_id),
        race=_get_race_name(request.race_id),
        predicted_position=predicted_pos,
        podium_probability=ml_result["podium_probability"],
        model_version=ml_result["model_version"],
        confidence_interval=[max(1, predicted_pos - 2), min(20, predicted_pos + 2)],
    )


@router.post("/predict/race/{race_id}", response_model=List[PredictionResponse])
def predict_race(
    race_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Classement prédit complet pour tous les pilotes engagés dans un GP."""
    try:
        from ml.predict import predict as ml_predict
    except ImportError:
        raise HTTPException(status_code=503, detail="Service ML non disponible")

    results_df = _load_csv("results")
    if results_df.empty:
        raise HTTPException(
            status_code=503,
            detail="Données F1 non disponibles. Placez les CSV Kaggle dans ml/data/raw/",
        )

    race_entries = results_df[results_df["raceId"] == race_id]
    if race_entries.empty:
        raise HTTPException(status_code=404, detail="Aucun pilote trouvé pour cette course")

    driver_ids = race_entries["driverId"].unique().tolist()
    race_name = _get_race_name(race_id)

    predictions = []
    for driver_id in driver_ids:
        features = _build_features(race_id, int(driver_id))
        try:
            ml_result = ml_predict(features)
        except FileNotFoundError:
            raise HTTPException(
                status_code=503,
                detail="Aucun modèle entraîné. Lancez POST /admin/train d'abord.",
            )
        except Exception:
            ml_result = {"podium_probability": 0.05, "model_version": "unknown"}

        predictions.append({
            "driver_id": int(driver_id),
            "driver": _get_driver_name(int(driver_id)),
            "podium_probability": ml_result["podium_probability"],
            "model_version": ml_result.get("model_version", "unknown"),
        })

    # Trier par probabilité décroissante et assigner les positions
    predictions.sort(key=lambda x: x["podium_probability"], reverse=True)

    response = []
    for rank, p in enumerate(predictions, start=1):
        response.append(PredictionResponse(
            driver=p["driver"],
            race=race_name,
            predicted_position=rank,
            podium_probability=p["podium_probability"],
            model_version=p["model_version"],
            confidence_interval=[max(1, rank - 2), min(len(predictions), rank + 2)],
        ))

    # Sauvegarder en batch dans prediction_log
    for rank, p in enumerate(predictions, start=1):
        log = PredictionLog(
            user_id=current_user.id,
            race_id=race_id,
            driver_id=p["driver_id"],
            predicted_position=rank,
            podium_probability=p["podium_probability"],
            model_version=p["model_version"],
        )
        db.add(log)
    db.commit()

    return response


@router.get("/predictions/history", response_model=List[PredictionResponse])
def get_history(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Historique des prédictions de l'utilisateur connecté."""
    logs = (
        db.query(PredictionLog)
        .filter(PredictionLog.user_id == current_user.id)
        .order_by(PredictionLog.created_at.desc())
        .all()
    )
    return [
        PredictionResponse(
            driver=_get_driver_name(log.driver_id),
            race=_get_race_name(log.race_id),
            predicted_position=log.predicted_position,
            podium_probability=log.podium_probability,
            model_version=log.model_version,
            real_position=log.real_position,
        )
        for log in logs
    ]
