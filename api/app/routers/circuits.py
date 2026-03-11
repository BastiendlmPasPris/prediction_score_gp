from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List

from app.db.database import get_db
from app.core.auth import get_current_user

router = APIRouter()


@router.get("")
def get_circuits(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    """Liste des circuits"""
    # TODO: Retourner la liste des circuits
    pass
