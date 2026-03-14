# F1 Predict

Application Android de prédiction de résultats de Formule 1 — Projet DevOps Mobile, Le Mans Université S2 2026.

## Architecture

```
f1-predict/
├── android/        → Application Android (Java + XML)
├── api/            → API REST (FastAPI + JWT)
├── ml/             → Pipeline ML (preprocessing + AutoML)
├── .github/        → CI/CD (GitHub Actions)
├── docker-compose.yml
└── .env.example
```

## Lancer le projet

### Prérequis
- Docker & Docker Compose
- Android Studio (pour le front)
- Python 3.11+

### Démarrer l'API + BDD
```bash
cp .env.example .env
docker-compose up api db
```
L'API est accessible sur http://localhost:8000
La doc Swagger est sur http://localhost:8000/docs

### Lancer l'entraînement ML
```bash
docker-compose run ml
# ou en local :
cd ml && python train.py
```

### Lancer les tests
```bash
cd api && pytest tests/ -v
```

## Stack technique

| Composant    | Technologies                              |
|--------------|-------------------------------------------|
| Front-end    | Java, XML, Retrofit, Material Design 3    |
| API          | Python, FastAPI, JWT, Swagger/OpenAPI     |
| ML           | scikit-learn, pandas, joblib, AutoML S1   |
| BDD          | SQLite (dev) / PostgreSQL (prod)          |
| DevOps       | GitHub Actions, Docker, pytest            |

## Équipe

| Membre | Rôle |
|--------|------|
| P1 | Front-end Android |
| P2 | API REST |
| P3 | ML / Back-end |
| P4 | DevOps + Documentation |

