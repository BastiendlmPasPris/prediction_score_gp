package com.example.prediction_score_gp.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prediction_score_gp.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // TODO: Bouton "Relancer l'entraînement" → POST /admin/train
        // TODO: Afficher les métriques (accuracy, F1, précision, rappel) en cards
        // TODO: Graphique matrice de confusion
        // TODO: Sélecteur version du modèle + bouton rollback
        // TODO: Afficher logs récents de l'API
        // TODO: Connecter au AdminViewModel
    }
}
