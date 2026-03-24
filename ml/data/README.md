# Données F1 — ml/data/raw/

Ce dossier contient les données brutes du projet F1 Predict.
Les fichiers CSV **ne sont pas versionnés** dans Git (voir `.gitignore`).

## Source des données

Dataset Kaggle : **Formula 1 World Championship (1950-2024)**  
Auteur : Rohan Rao (Vopani)  
URL : https://www.kaggle.com/datasets/rohanrao/formula-1-world-championship-1950-2020

## Téléchargement

### Option 1 — Via Kaggle CLI (recommandée)
```bash
# Installer le client Kaggle
pip install kaggle

# Configurer votre clé API (https://www.kaggle.com/settings/account → Create New Token)
# Placer le fichier ~/.kaggle/kaggle.json

# Télécharger et décompresser dans le bon dossier
kaggle datasets download rohanrao/formula-1-world-championship-1950-2020 \
  --path ml/data/raw --unzip
```

### Option 2 — Téléchargement manuel
1. Aller sur https://www.kaggle.com/datasets/rohanrao/formula-1-world-championship-1950-2020
2. Cliquer sur **Download**
3. Décompresser l'archive dans `ml/data/raw/`

## Fichiers requis

Placer les fichiers suivants dans `ml/data/raw/` :

| Fichier | Description |
|---------|-------------|
| `results.csv` | Résultats de course (position finale, points, temps) — **table centrale** |
| `races.csv` | Liste des GP par saison (date, circuit, saison) |
| `drivers.csv` | Informations pilotes (nom, nationalité, date de naissance) |
| `constructors.csv` | Informations écuries |
| `qualifying.csv` | Résultats des qualifications (position grille) |
| `circuits.csv` | Circuits avec coordonnées GPS et pays |
| `driver_standings.csv` | Classements pilotes par saison |
| `constructor_standings.csv` | Classements constructeurs par saison |
| `constructor_results.csv` | Résultats constructeurs par course |
| `status.csv` | États de fin de course (Finished, DNF, DSQ...) |

## Vérification

Après téléchargement, tester le chargement depuis la racine du projet :

```bash
cd prediction_score_gp
python -c "
from ml.preprocessing.loader import load_all, merge_tables
dfs = load_all()
print('CSV chargés:', list(dfs.keys()))
df = merge_tables(dfs)
print('Shape après merge:', df.shape)
# Attendu : > 15 000 lignes (données 2010-2024)
"
```

## Note

Les données antérieures à 2010 sont filtrées automatiquement par `merge_tables()` car
elles sont moins représentatives des règles modernes de la F1.
