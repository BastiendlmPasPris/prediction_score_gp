from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from sqlalchemy.sql import func
from app.db.database import Base


class PredictionLog(Base):
    __tablename__ = "prediction_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    race_id = Column(Integer, nullable=False)
    driver_id = Column(Integer, nullable=False)
    predicted_position = Column(Integer, nullable=False)
    podium_probability = Column(Float, nullable=False)
    model_version = Column(String, nullable=False)
    real_position = Column(Integer, nullable=True)  # rempli après la course
    created_at = Column(DateTime(timezone=True), server_default=func.now())
