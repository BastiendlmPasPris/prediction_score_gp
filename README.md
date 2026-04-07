# F1 Predict

[![CI - F1 Predict](https://github.com/BastiendlmPasPris/prediction_score_gp/actions/workflows/ci.yml/badge.svg)](https://github.com/BastiendlmPasPris/prediction_score_gp/actions/workflows/ci.yml)

Application Android de prédiction de résultats de Formule 1, couplée à une API REST FastAPI et un pipeline Machine Learning scikit-learn.  
Projet DevOps Mobile — Le Mans Université, S2 2026.

---

## Présentation

**F1 Predict** permet à un utilisateur authentifié de prédire les résultats d'un Grand Prix de Formule 1 grâce à un modèle de Machine Learning entraîné sur l'historique F1 depuis 1950 jusqu'en 2024.

L'utilisateur choisit une course et un pilote, et obtient :
- une **probabilité de podium** (0 % → 100 %)
- une **position estimée** (1 → 20)
- un **classement complet** du Grand Prix (tous les pilotes)

---

## Fonctionnalités de l'application Android

L'application est organisée en **4 onglets navigables par swipe ou tap** :

### Globe
- Globe 3D interactif rendu avec **Three.js** dans une WebView
- Les 22 circuits du calendrier 2026 sont représentés par des **marqueurs rouges pulsants**
- Tap sur un circuit → affichage d'une fiche course avec circuit, date, bouton "PREDICT"
- Contrôle de la rotation automatique (pause/play)
- Slider d'opacité de la texture terrestre
- **Mode gyroscope** : la planète suit les mouvements physiques du téléphone (capteur `TYPE_ROTATION_VECTOR`)

### Predict
- Liste de tous les Grands Prix de la saison 2026
- Tap sur une course → BottomSheet avec sélection du pilote (grille 2026 complète)
- Résultat affiché en Toast : position estimée + probabilité de podium
- **Vibration double impulsion** au moment de lancer une prédiction

### Podium
- Sélection d'un Grand Prix via un Spinner
- Affichage du **podium prédit** (P1 or / P2 argent / P3 bronze) avec couleurs d'écurie
- Classement complet des pilotes restants (P4 → P22) dans un RecyclerView
- Tap sur un pilote → BottomSheet avec ses statistiques (victoires, podiums, poles, probabilité)

### Driver
- Profil de l'utilisateur connecté (nom, email, rôle)
- Historique des prédictions effectuées
- Bouton de déconnexion

---

## Architecture du projet

```
prediction_score_gp/
├── app/                            → Application Android (Java + XML)
│   └── src/main/
│       ├── java/.../
│       │   ├── ui/
│       │   │   ├── auth/           → LoginActivity, RegisterActivity
│       │   │   ├── dashboard/      → GlobeFragment (globe 3D + gyroscope)
│       │   │   ├── prediction/     → PredictFragment, RaceBottomSheet
│       │   │   ├── standings/      → PodiumFragment, DriverBottomSheet
│       │   │   └── profile/        → DriverFragment (profil utilisateur)
│       │   ├── viewmodel/          → DashboardViewModel, PredictionViewModel,
│       │   │                          RaceViewModel, SharedViewModel
│       │   ├── data/api/           → DriversApi, RacesApi, PredictApi, AuthApi
│       │   └── data/model/         → Driver, Race, Prediction, User
│       ├── assets/
│       │   ├── index.html          → Globe Three.js (rendu WebGL)
│       │   └── earth.jpg           → Texture terrestre
│       └── res/layout/
│           ├── activity_main.xml   → ViewPager2 + barre de navigation
│           ├── fragment_globe.xml
│           ├── fragment_predict.xml
│           ├── fragment_podium.xml
│           └── fragment_driver.xml
├── api/                            → API REST (Python / FastAPI)
│   └── app/
│       ├── routers/
│       │   ├── auth.py             → POST /auth/register, /auth/login, GET /auth/me
│       │   ├── drivers.py          → GET /drivers, /drivers/{id}, /drivers/{id}/stats
│       │   ├── races.py            → GET /races, /races/{id}
│       │   ├── predict.py          → POST /predict, /predict/race/{id}, GET /predictions/history
│       │   ├── admin.py            → POST /admin/train, GET /admin/eval, /admin/stats
│       │   ├── circuits.py         → GET /circuits
│       │   └── constructors.py     → GET /constructors
│       ├── models/                 → Tables SQLAlchemy (User, PredictionLog)
│       ├── schemas/                → Validation Pydantic (entrées/sorties API)
│       ├── core/                   → config.py (variables d'env), auth.py (JWT)
│       └── db/                     → database.py (engine SQLAlchemy, sessions)
├── ml/                             → Pipeline Machine Learning (Python)
│   ├── preprocessing/
│   │   ├── loader.py               → Chargement et fusion des CSV Kaggle
│   │   ├── pipeline.py             → Orchestration du feature engineering
│   │   └── features.py             → Fonctions de calcul des features
│   ├── automl/
│   │   └── automl.py               → Sélection automatique du meilleur modèle
│   ├── evaluation/
│   │   └── metrics.py              → Calcul et sauvegarde des métriques
│   ├── models/versions/            → Modèles .joblib versionnés
│   ├── data/raw/                   → CSV Kaggle (à placer manuellement)
│   ├── train.py                    → Entraînement complet du modèle
│   └── predict.py                  → Inférence (appelé par l'API)
├── .github/workflows/ci.yml        → Pipeline CI/CD GitHub Actions
├── docker-compose.yml              → Orchestration des services
└── .env.example                    → Template des variables d'environnement
```

---

## Stack technique

| Composant | Technologies |
|---|---|
| Application Android | Java, XML, ViewPager2, Retrofit 2, Material Design 3, MVVM, Three.js |
| Capteurs Android | `TYPE_ROTATION_VECTOR` (gyroscope), `VibrationEffect` |
| API REST | Python 3.11, FastAPI, SQLAlchemy, Pydantic, python-jose (JWT), bcrypt |
| Machine Learning | scikit-learn, pandas, numpy, joblib |
| Base de données | SQLite (développement) / PostgreSQL 16 (production) |
| DevOps | Docker, Docker Compose, GitHub Actions, pytest |

---

## Données F1 requises

Le modèle ML et l'API utilisent les données historiques F1 du dataset Kaggle :

**Source** : [Formula 1 World Championship (1950–2024)](https://www.kaggle.com/datasets/rohanrao/formula-1-world-championship-1950-2020)

Après téléchargement, placer les CSV dans `ml/data/raw/` :

| Fichier | Contenu |
|---|---|
| `results.csv` | Résultats de toutes les courses (position, points, statut) |
| `races.csv` | Calendrier de toutes les saisons |
| `drivers.csv` | Informations sur tous les pilotes (nom, nationalité, date de naissance) |
| `constructors.csv` | Informations sur les écuries |
| `qualifying.csv` | Résultats des qualifications (poles) |
| `circuits.csv` | Informations sur les circuits (pays, coordonnées) |
| `status.csv` | Codes de statut (Finished, Accident, DNF…) |

---

## Pipeline Machine Learning

### 1. Chargement des données (`loader.py`)
- Lecture des CSV Kaggle
- Jointures entre résultats, courses, pilotes, écuries, circuits
- Filtrage sur l'ère moderne F1 (depuis 2010)

### 2. Feature Engineering (`features.py`)
Calculées par pilote et par course :

| Feature | Description |
|---|---|
| `driver_age` | Âge du pilote au jour de la course |
| `dnf_rate_last10` | Taux d'abandon sur les 10 dernières courses |
| `driver_podiums_last5` | Nombre de podiums sur les 5 dernières courses |
| `circuit_history_avg` | Position moyenne du pilote sur ce circuit |
| `home_race` | 1 si le pilote court dans son pays natal |
| `constructor_encoded` | Écurie encodée (LabelEncoder) |

### 3. Cible
`podium` → 1 si position finale ≤ 3, sinon 0 (classification binaire)

### 4. AutoML (`automl.py`)
Comparaison de 5 algorithmes par validation croisée StratifiedKFold (5 folds) :

| Modèle | Paramètres clés |
|---|---|
| RandomForest | 200 arbres, profondeur max 10, class_weight=balanced |
| GradientBoosting | 150 estimateurs, profondeur 5 |
| LogisticRegression | StandardScaler + max_iter=1000 |
| KNN | k=7, poids par distance |
| SVM | Kernel RBF, probability=True |

Le modèle retenu est celui avec le **F1-score le plus élevé**.

### 5. Inférence
La probabilité de podium est convertie en position estimée :

```
proba ≥ 0.70  →  P1
proba ≥ 0.50  →  P2
proba ≥ 0.35  →  P3
sinon         →  P4 à P20 (interpolation linéaire)
```

---

## Endpoints API

Tous les endpoints (sauf `/auth/register` et `/auth/login`) nécessitent un **token JWT** en header :  
`Authorization: Bearer <token>`

### Authentification
| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Créer un compte |
| POST | `/auth/login` | Se connecter, reçoit le JWT |
| GET | `/auth/me` | Infos de l'utilisateur connecté |
| PUT | `/auth/me` | Modifier son profil |

### Données
| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/races?season=2026` | Liste des courses (filtre par saison) |
| GET | `/races/{id}` | Détail d'une course |
| GET | `/drivers` | Liste de tous les pilotes |
| GET | `/drivers/{id}` | Détail d'un pilote |
| GET | `/drivers/{id}/stats` | Statistiques (victoires, podiums, poles) |
| GET | `/circuits` | Liste des circuits |
| GET | `/constructors` | Liste des écuries |

### Prédictions
| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/predict` | Prédiction pilote + course (`race_id`, `driver_id`) |
| POST | `/predict/race/{id}` | Classement complet du Grand Prix |
| GET | `/predictions/history` | Historique des prédictions de l'utilisateur |

### Administration (rôle admin)
| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/admin/train` | Entraîner le modèle ML |
| GET | `/admin/eval` | Métriques du modèle (accuracy, F1, recall) |
| GET | `/admin/model/versions` | Liste des versions sauvegardées |
| POST | `/admin/model/rollback/{version}` | Revenir à une version précédente |
| GET | `/admin/stats` | Statistiques d'utilisation de l'API |

---

## Installation et lancement

### Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installé et lancé
- [Android Studio](https://developer.android.com/studio)
- [Python 3.11](https://www.python.org/) (pour les tests en local)
- Git

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

### 3. Placer les données Kaggle

Télécharger le dataset [Formula 1 World Championship](https://www.kaggle.com/datasets/rohanrao/formula-1-world-championship-1950-2020) et extraire les CSV dans `ml/data/raw/`.

### 4. Lancer l'API et la base de données

```bash
docker-compose up api db
```

- API : [http://localhost:8000](http://localhost:8000)
- Documentation Swagger : [http://localhost:8000/docs](http://localhost:8000/docs)

> La première exécution télécharge les images Docker (2-3 min). Les suivantes sont quasi instantanées.

### 5. Entraîner le modèle ML

```bash
docker-compose run ml
# ou en local :
cd ml && python train.py
```

Le modèle est sauvegardé dans `ml/models/versions/`.

### 6. Lancer l'application Android

1. Ouvrir Android Studio
2. Ouvrir le dossier racine du projet
3. Lancer sur un émulateur ou un appareil physique
4. L'app se connecte à `http://10.0.2.2:8000` (localhost depuis l'émulateur Android)

---

## Tests

### Préparer l'environnement

```bash
conda create -n f1predict python=3.11
conda activate f1predict
cd api && pip install -r requirements.txt
```

### Lancer les tests

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
| `test_predict_full_race` | Classement complet d'un GP |
| `test_predict_without_token` | Rejet sans authentification (401) |
| `test_train_without_token` | Protection route admin `/train` (401) |
| `test_eval_without_token` | Protection route admin `/eval` (401) |
| `test_model_versions_without_token` | Protection route admin `/versions` (401) |

---

## Base de données

### Développement — SQLite (par défaut)

```
DATABASE_URL=sqlite:///./f1predict.db
```

Aucune configuration requise. Le fichier `.db` est créé automatiquement au démarrage.

### Production — PostgreSQL (Docker)

```
DATABASE_URL=postgresql://f1predict:f1predict_pass@db:5432/f1predict
```

| Paramètre | Valeur |
|---|---|
| Host | `localhost` |
| Port | `5433` |
| User | `f1predict` |
| Password | `f1predict_pass` |
| Database | `f1predict` |

> Le port `5433` est utilisé pour ne pas entrer en conflit avec un PostgreSQL installé nativement.

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

Le pipeline GitHub Actions se déclenche à chaque push sur `master` ou `develop`, et sur chaque Pull Request.

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
