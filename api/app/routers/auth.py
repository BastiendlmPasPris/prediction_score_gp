from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.schemas.auth import UserCreate, UserLogin, UserResponse
from app.core.auth import create_access_token, get_current_user

router = APIRouter()


@router.post("/register", response_model=UserResponse, status_code=201)
def register(user: UserCreate, db: Session = Depends(get_db)):
    """Inscription d'un nouvel utilisateur"""
    # TODO: Vérifier que l'email n'existe pas déjà
    # TODO: Hasher le mot de passe (bcrypt)
    # TODO: Créer l'utilisateur en BDD
    # TODO: Retourner le token JWT
    pass


@router.post("/login", response_model=UserResponse)
def login(user: UserLogin, db: Session = Depends(get_db)):
    """Connexion et retour d'un token JWT"""
    # TODO: Vérifier email + mot de passe hashé
    # TODO: Générer et retourner le JWT
    pass


@router.get("/me", response_model=UserResponse)
def get_me(current_user=Depends(get_current_user)):
    """Retourne les infos de l'utilisateur connecté"""
    return current_user


@router.put("/me", response_model=UserResponse)
def update_me(current_user=Depends(get_current_user), db: Session = Depends(get_db)):
    """Modifier le profil de l'utilisateur connecté"""
    # TODO: Mettre à jour les champs modifiables
    pass
