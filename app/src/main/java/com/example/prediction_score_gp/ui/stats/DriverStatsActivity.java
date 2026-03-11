package com.example.prediction_score_gp.ui.stats;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prediction_score_gp.R;

public class DriverStatsActivity extends AppCompatActivity {

    public static final String EXTRA_DRIVER_ID = "driver_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_stats);

        int driverId = getIntent().getIntExtra(EXTRA_DRIVER_ID, -1);

        // TODO: Afficher photo, nom, nationalité, écurie actuelle
        // TODO: Afficher wins / podiums / poles
        // TODO: Graphique des 10 dernières courses (MPAndroidChart)
        // TODO: Tableau des performances par circuit
        // TODO: Connecter au StatsViewModel
    }
}
