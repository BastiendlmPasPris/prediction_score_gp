import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from passlib.context import CryptContext

from app.main import app
from app.db.database import Base, get_db
from app.models.user import User
from app.core.auth import create_access_token

# Base de données SQLite dédiée aux tests
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


@pytest.fixture(autouse=True)
def setup_db():
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


def override_get_db():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db


@pytest.fixture
def db():
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()


@pytest.fixture
def client():
    with TestClient(app) as c:
        yield c


@pytest.fixture
def test_user(db):
    """Crée un utilisateur standard en BDD de test"""
    user = User(
        email="test@f1predict.com",
        username="testuser",
        hashed_password=pwd_context.hash("password123"),
        role="user",
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@pytest.fixture
def admin_user(db):
    """Crée un administrateur en BDD de test"""
    user = User(
        email="admin@f1predict.com",
        username="admin",
        hashed_password=pwd_context.hash("admin123"),
        role="admin",
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@pytest.fixture
def auth_token(client):
    """Inscription + login d'un utilisateur de test, retourne le token JWT"""
    client.post("/auth/register", json={
        "email": "test@f1predict.com",
        "username": "testuser",
        "password": "password123",
    })
    response = client.post("/auth/login", json={
        "email": "test@f1predict.com",
        "password": "password123",
    })
    return response.json()["token"]


@pytest.fixture
def admin_token(admin_user):
    """Retourne un token JWT admin directement généré (sans passer par l'API)"""
    return create_access_token({"sub": str(admin_user.id), "role": admin_user.role})


@pytest.fixture
def auth_headers(auth_token):
    return {"Authorization": f"Bearer {auth_token}"}


@pytest.fixture
def admin_headers(admin_token):
    return {"Authorization": f"Bearer {admin_token}"}
