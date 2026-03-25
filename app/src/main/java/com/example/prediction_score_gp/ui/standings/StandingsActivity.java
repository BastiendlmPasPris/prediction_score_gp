package com.example.prediction_score_gp.ui.standings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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


        setupNavBar();
    }

    private void setupNavBar() {
        int activeColor   = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        int[][] tabs = {
                {R.id.tabGlobe,   R.id.iconRaces,   R.id.labelRaces},
                {R.id.tabPredict, R.id.iconPredict,  R.id.labelPredict},
                {R.id.tabPodium,  R.id.iconPodium,   R.id.labelPodium},
                {R.id.tabDriver,  R.id.iconDriver,   R.id.labelDriver},
        };

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            ImageView icon    = findViewById(tab[1]);
            TextView label   = findViewById(tab[2]);

            tabView.setOnClickListener(v -> {
                for (int[] t : tabs) {
                    ((ImageView) findViewById(t[1])).setColorFilter(inactiveColor);
                    ((TextView)  findViewById(t[2])).setTextColor(inactiveColor);
                }
                icon.setColorFilter(activeColor);
                label.setTextColor(activeColor);

                // TODO : navigation entre fragments
                if (tab[0] == R.id.tabPredict) {
                    setContentView(R.layout.activity_prediction);
                } else if (tab[0] == R.id.tabGlobe) {
                    setContentView(R.layout.activity_dashboard);
                } else if (tab[0] == R.id.tabDriver) {
                    setContentView(R.layout.activity_profile);
                }
            });
        }
    }
}
