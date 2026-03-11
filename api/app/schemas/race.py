from pydantic import BaseModel
from typing import Optional


class RaceResponse(BaseModel):
    id: int
    name: str
    circuit: str
    country: str
    date: str
    season: int
    flag_url: Optional[str] = None

    class Config:
        from_attributes = True
