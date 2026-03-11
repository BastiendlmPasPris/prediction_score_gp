package com.example.prediction_score_gp.ui.standings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prediction_score_gp.R;

public class StandingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standings);

        // TODO: Afficher la RecyclerView des 20 pilotes classés
        // TODO: Mettre en évidence les 3 premiers (or, argent, bronze)
        // TODO: Chaque ligne : position, photo, nom, écurie (couleur équipe), proba podium
        // TODO: Connecter au StandingsViewModel
    }
}
