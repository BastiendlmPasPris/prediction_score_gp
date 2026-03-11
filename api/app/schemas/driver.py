from pydantic import BaseModel
from typing import Optional, List


class DriverResponse(BaseModel):
    id: int
    first_name: str
    last_name: str
    nationality: str
    team: str
    photo_url: Optional[str] = None

    class Config:
        from_attributes = True


class DriverStatsResponse(BaseModel):
    driver_id: int
    wins: int
    podiums: int
    poles: int
    last_10_results: List[int]          # positions des 10 dernières courses
    circuit_performances: dict           # {circuit_name: avg_position}
