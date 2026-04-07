from datetime import datetime, timedelta
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials 
from jose import JWTError, jwt

from app.core.config import settings

security = HTTPBearer()

SECRET_KEY = settings.JWT_SECRET
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24  # 24h


def create_access_token(data: dict) -> str:
    """Génère un token JWT"""
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def decode_token(token: str) -> dict:
    """Décode et valide un token JWT"""
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalide ou expiré"
        )


def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)): # <-- CHANGEMENT ICI
    """Middleware : vérifie le JWT et retourne l'utilisateur"""
    token = credentials.credentials # <-- Récupération du token
    payload = decode_token(token)
    from app.db.database import SessionLocal
    from app.models.user import User
    db = SessionLocal()
    try:
        user = db.query(User).filter(User.id == int(payload["sub"])).first()
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Utilisateur introuvable",
            )
        return user
    finally:
        db.close()


def get_current_admin(credentials: HTTPAuthorizationCredentials = Depends(security)): # <-- CHANGEMENT ICI
    """Middleware : vérifie le JWT et que l'utilisateur est admin"""
    token = credentials.credentials # <-- Récupération du token
    payload = decode_token(token)
    if payload.get("role") != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Accès réservé aux administrateurs"
        )
    return payload
