from pydantic import BaseModel
from typing import List, Optional


class PredictRequest(BaseModel):
    race_id: int
    driver_id: int


class PredictionResponse(BaseModel):
    driver: str
    race: str
    predicted_position: int
    podium_probability: float
    model_version: str
    confidence_interval: Optional[List[int]] = None
    real_position: Optional[int] = None  # rempli après la course
