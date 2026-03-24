from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import auth, races, drivers, constructors, circuits, predict, admin
from app.db.database import Base, engine
from app.models import user, prediction_log  # noqa: F401 — nécessaire pour que SQLAlchemy enregistre les tables

# Création des tables en base
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="F1 Predict API",
    description="API REST pour l'application de prédiction de résultats F1",
    version="1.0.0"
)

# CORS (autoriser les appels depuis l'émulateur Android)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inclusion des routers
app.include_router(auth.router,         prefix="/auth",         tags=["Authentification"])
app.include_router(races.router,        prefix="/races",        tags=["Courses"])
app.include_router(drivers.router,      prefix="/drivers",      tags=["Pilotes"])
app.include_router(constructors.router, prefix="/constructors", tags=["Ecuries"])
app.include_router(circuits.router,     prefix="/circuits",     tags=["Circuits"])
app.include_router(predict.router,      prefix="",             tags=["Prédictions"])
app.include_router(admin.router,        prefix="/admin",        tags=["Administration"])


@app.get("/")
def root():
    return {"message": "F1 Predict API opérationnelle"}
