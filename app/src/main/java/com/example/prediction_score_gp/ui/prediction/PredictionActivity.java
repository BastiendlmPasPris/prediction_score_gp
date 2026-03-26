package com.example.prediction_score_gp.ui.prediction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;
import com.example.prediction_score_gp.viewmodel.DashboardViewModel;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;

import java.util.ArrayList;

public class PredictionActivity extends AppCompatActivity {

    private RaceAdapter raceAdapter;
    private DashboardViewModel dashboardViewModel;
    private PredictionViewModel predictionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_prediction);

        setupWindowInsets();
        setupRecyclerView();
        setupViewModels();
        setupNavBar();

        // Charger les courses de la saison en cours
        dashboardViewModel.loadRaces(2024);
    }

    // ── WindowInsets ─────────────────────────────────────────────────
    private void setupWindowInsets() {
        View rootLayout   = findViewById(R.id.rootLayout);
        View statusSpacer = findViewById(R.id.statusBarSpacer);
        View navBarSpacer = findViewById(R.id.navBarSpacer);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (statusSpacer != null) {
                statusSpacer.getLayoutParams().height = sysBars.top;
                statusSpacer.requestLayout();
            }
            if (navBarSpacer != null) {
                navBarSpacer.getLayoutParams().height = sysBars.bottom;
                navBarSpacer.requestLayout();
            }
            return insets;
        });
    }

    // ── RecyclerView ─────────────────────────────────────────────────
    private void setupRecyclerView() {
        RecyclerView rvRaces = findViewById(R.id.rvRaces);
        if (rvRaces == null) return;

        raceAdapter = new RaceAdapter(new ArrayList<>(), this::openRaceSheet);
        rvRaces.setLayoutManager(new LinearLayoutManager(this));
        rvRaces.setAdapter(raceAdapter);
    }

    // ── ViewModels ────────────────────────────────────────────────────
    private void setupViewModels() {
        dashboardViewModel  = new ViewModelProvider(this).get(DashboardViewModel.class);
        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);

        dashboardViewModel.racesLiveData.observe(this, races -> {
            if (races != null) raceAdapter.updateData(races);
        });

        dashboardViewModel.errorLiveData.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        predictionViewModel.predictionLiveData.observe(this, prediction -> {
            if (prediction != null) {
                String msg = prediction.getDriver()
                        + " → position " + prediction.getPredictedPosition()
                        + " (" + Math.round(prediction.getPodiumProbability() * 100) + "% podium)";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        predictionViewModel.errorLiveData.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Ouvrir le BottomSheet ────────────────────────────────────────
    private void openRaceSheet(Race race) {
        RaceBottomSheet sheet = RaceBottomSheet.newInstance(race);

        sheet.setOnPredictListener((selectedRace, driverName) ->
                predictionViewModel.predict(selectedRace.getId(), 0));  // driver_id résolu via liste

        sheet.show(getSupportFragmentManager(), RaceBottomSheet.class.getSimpleName());
    }

    // ── Navbar ───────────────────────────────────────────────────────
    private void setupNavBar() {
        int activeColor   = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        int[][] tabs = {
                {R.id.tabGlobe,   R.id.iconGlobe,   R.id.labelGlobe},
                {R.id.tabPredict, R.id.iconPredict,  R.id.labelPredict},
                {R.id.tabPodium,  R.id.iconPodium,   R.id.labelPodium},
                {R.id.tabDriver,  R.id.iconDriver,   R.id.labelDriver},
        };

        ((ImageView) findViewById(R.id.iconPredict)).setColorFilter(activeColor);
        ((TextView)  findViewById(R.id.labelPredict)).setTextColor(activeColor);

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                if (tab[0] == R.id.tabPredict) return;

                for (int[] t : tabs) {
                    ImageView img = findViewById(t[1]);
                    TextView  txt = findViewById(t[2]);
                    if (img != null) img.setColorFilter(inactiveColor);
                    if (txt != null) txt.setTextColor(inactiveColor);
                }
                ((ImageView) findViewById(tab[1])).setColorFilter(activeColor);
                ((TextView)  findViewById(tab[2])).setTextColor(activeColor);

                Class<?> target = null;
                if (tab[0] == R.id.tabGlobe)  target = DashboardActivity.class;
                if (tab[0] == R.id.tabPodium) target = StandingsActivity.class;
                if (tab[0] == R.id.tabDriver) target = ProfileActivity.class;

                if (target != null) {
                    startActivity(new Intent(this, target));
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }
}
