package com.example.prediction_score_gp.ui.prediction;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prediction_score_gp.R;

public class PredictionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        // TODO: Initialiser le sélecteur de GP (Spinner avec drapeaux)
        // TODO: Initialiser le sélecteur de pilote (photo + écurie)
        // TODO: Bouton "Prédire" → appel POST /predict
        // TODO: Afficher résultat : position prédite + jauge probabilité podium
        // TODO: Afficher historique du pilote sur ce circuit
        // TODO: Connecter au PredictionViewModel
    }
}
