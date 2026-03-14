import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.main import app
from app.db.database import Base, get_db

# Base de données SQLite en mémoire (isolée, zéro config)
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def override_get_db():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()

@pytest.fixture(autouse=True)
def setup_db():
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)

app.dependency_overrides[get_db] = override_get_db

@pytest.fixture
def client():
    return TestClient(app)

@pytest.fixture
def auth_token(client):
    """Crée un utilisateur de test et retourne son token JWT"""
    client.post("/auth/register", json={
        "email": "test@f1predict.com",
        "username": "testuser",
        "password": "password123"
    })
    response = client.post("/auth/login", json={
        "email": "test@f1predict.com",
        "password": "password123"
    })
    return response.json()["token"]
