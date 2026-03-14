# F1 Predict

[![CI - F1 Predict](https://github.com/BastiendlmPasPris/prediction_score_gp/actions/workflows/ci.yml/badge.svg)](https://github.com/BastiendlmPasPris/prediction_score_gp/actions/workflows/ci.yml)

Application Android de prédiction de résultats de Formule 1.
Projet DevOps Mobile — Le Mans Université, S2 2026.

---

## Présentation

F1 Predict permet à un utilisateur de prédire les résultats d'un Grand Prix de Formule 1 grâce à un modèle de Machine Learning entraîné sur l'historique F1 depuis 1950. L'utilisateur choisit une course et un pilote, et obtient une probabilité de podium ainsi qu'une position prédite.

---

## Architecture du projet

```
prediction_score_gp/
├── app/                        → Application Android (Java + XML)
│   └── src/main/java/.../
│       ├── ui/                 → Écrans (Login, Dashboard, Prediction...)
│       ├── viewmodel/          → Logique métier (MVVM)
│       ├── data/api/           → Appels HTTP (Retrofit)
│       ├── data/model/         → Objets de données (User, Race, Driver...)
│       └── data/repository/    → Intermédiaires ViewModel ↔ API
├── api/                        → API REST (Python / FastAPI)
│   └── app/
│       ├── routers/            → Endpoints HTTP (/auth, /predict, /admin...)
│       ├── models/             → Tables base de données (SQLAlchemy)
│       ├── schemas/            → Validation des données (Pydantic)
│       ├── core/               → JWT, configuration
│       └── db/                 → Connexion base de données
├── ml/                         → Pipeline Machine Learning (Python)
│   ├── preprocessing/          → Chargement CSV, feature engineering
│   ├── automl/                 → Comparaison et sélection de modèles
│   ├── evaluation/             → Métriques de performance
│   ├── train.py                → Entraînement et sauvegarde du modèle
│   └── predict.py              → Inférence (utilisé par l'API)
├── .github/workflows/          → Pipeline CI/CD (GitHub Actions)
├── docker-compose.yml          → Orchestration des services
└── .env.example                → Template des variables d'environnement
```

---

## Stack technique

| Composant | Technologies |
|---|---|
| Application Android | Java, XML, Retrofit, Material Design 3, MVVM |
| API REST | Python 3.11, FastAPI, SQLAlchemy, JWT (python-jose) |
| Machine Learning | scikit-learn, pandas, numpy, joblib |
| Base de données | SQLite (développement) / PostgreSQL 16 (production) |
| DevOps | Docker, Docker Compose, GitHub Actions, pytest |

---

## Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installé et lancé
- [Android Studio](https://developer.android.com/studio) (pour lancer l'app Android)
- [Python 3.11](https://www.python.org/) (pour lancer les tests en local)
- Git

---

## Installation et lancement

### 1. Cloner le projet

```bash
git clone https://github.com/BastiendlmPasPris/prediction_score_gp.git
cd prediction_score_gp
```

### 2. Configurer les variables d'environnement

```bash
cp .env.example .env
```

Le fichier `.env` contient des valeurs par défaut fonctionnelles. En production, remplacer `JWT_SECRET` par une vraie clé secrète.

### 3. Lancer l'API et la base de données

```bash
docker-compose up api db
```

- API disponible sur : [http://localhost:8000](http://localhost:8000)
- Documentation Swagger : [http://localhost:8000/docs](http://localhost:8000/docs)

> La première exécution télécharge les images Docker (2-3 minutes). Les suivantes sont quasi instantanées.

### 4. Lancer le service ML (entraînement)

```bash
docker-compose run ml
# ou en local :
cd ml && python train.py
```

### 5. Lancer l'application Android

1. Ouvrir Android Studio
2. Ouvrir le dossier racine du projet
3. Lancer sur un émulateur ou un appareil physique
4. L'app se connecte à `http://10.0.2.2:8000` (adresse de localhost depuis l'émulateur Android)

---

## Lancer les tests

### Préparer l'environnement Python 3.11

```bash
conda create -n f1predict python=3.11
conda activate f1predict
cd api
pip install -r requirements.txt
```

> Si l'environnement existe déjà, faire simplement `conda activate f1predict`.

### Exécuter les tests

```bash
python -m pytest tests/ -v
```

### Tests disponibles

| Test | Description |
|---|---|
| `test_register` | Inscription d'un utilisateur |
| `test_login` | Connexion et récupération du JWT |
| `test_login_wrong_password` | Rejet d'un mauvais mot de passe (401) |
| `test_get_me_without_token` | Rejet sans authentification (401) |
| `test_predict_single_driver` | Prédiction pour un pilote sur un GP |
| `test_predict_full_race` | Classement complet d'un GP (20 pilotes) |
| `test_predict_without_token` | Rejet sans authentification (401) |
| `test_train_without_token` | Protection de la route admin /train (401) |
| `test_eval_without_token` | Protection de la route admin /eval (401) |
| `test_model_versions_without_token` | Protection de la route admin /versions (401) |

---

## Base de données

### Développement (SQLite)

Par défaut, le fichier `.env` utilise SQLite — aucune configuration requise :
```
DATABASE_URL=sqlite:///./f1predict.db
```
Le fichier `.db` est créé automatiquement au démarrage de l'API.

### Production (PostgreSQL via Docker)

Modifier la variable dans `.env` :
```
DATABASE_URL=postgresql://f1predict:f1predict_pass@db:5432/f1predict
```
Puis relancer :
```bash
docker-compose up api db
```

PostgreSQL est accessible depuis la machine hôte sur le port `5433` :

| Paramètre | Valeur |
|---|---|
| Host | `localhost` |
| Port | `5433` |
| User | `f1predict` |
| Password | `f1predict_pass` |
| Database | `f1predict` |

> Le port `5433` est utilisé à la place de `5432` car PostgreSQL est déjà installé nativement sur la machine de développement.

---

## Variables d'environnement

| Variable | Description | Valeur par défaut |
|---|---|---|
| `JWT_SECRET` | Clé secrète pour signer les tokens JWT | `f1predict_secret_change_in_prod` |
| `DATABASE_URL` | URL de connexion à la base de données | `sqlite:///./f1predict.db` |
| `ML_MODELS_PATH` | Chemin vers les modèles ML sauvegardés | `../ml/models/versions` |
| `DEBUG` | Mode debug de l'API | `true` |

---

## CI/CD

Le pipeline GitHub Actions se déclenche automatiquement à chaque push sur `master` ou `develop`, et sur chaque Pull Request.

| Job | Description |
|---|---|
| Tests API (pytest) | Lance tous les tests unitaires |
| Lint Python (flake8) | Vérifie le style du code Python |
| Build Docker API | Vérifie que l'image Docker de l'API se construit |
| Build Docker ML | Vérifie que l'image Docker du ML se construit |

---

## Stratégie de branches

| Branche | Rôle |
|---|---|
| `master` | Code stable et validé — protégé, Pull Request obligatoire |
| `develop` | Branche d'intégration — reçoit les features terminées |
| `feature/xxx` | Une branche par fonctionnalité |

**Flux de travail :**
```
feature/xxx  →  Pull Request  →  develop  →  Pull Request (fin de sprint)  →  master
```

---

## Équipe

| Membre | Rôle |
|---|---|
| P1 | Front-end Android |
| P2 | API REST |
| P3 | Machine Learning |
| P4 | DevOps & Documentation |
