package com.example.prediction_score_gp.ui.standings;

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
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.viewmodel.DashboardViewModel;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;

import java.util.ArrayList;
import java.util.List;

public class StandingsActivity extends AppCompatActivity {

    private StandingsAdapter adapter;
    private PredictionViewModel predictionViewModel;
    private DashboardViewModel dashboardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_standings);

        setupWindowInsets();
        setupRecyclerView();
        setupViewModels();
        setupNavBar();

        // Charger la dernière saison disponible puis prédire
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
        RecyclerView rvStandings = findViewById(R.id.rvStandings);
        if (rvStandings == null) return;

        adapter = new StandingsAdapter(new ArrayList<>(), this::openDriverSheet);
        rvStandings.setLayoutManager(new LinearLayoutManager(this));
        rvStandings.setAdapter(adapter);
    }

    // ── ViewModels ────────────────────────────────────────────────────
    private void setupViewModels() {
        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        dashboardViewModel  = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Quand les courses sont chargées, prédire pour la course la plus récente
        dashboardViewModel.racesLiveData.observe(this, races -> {
            if (races != null && !races.isEmpty()) {
                int lastRaceId = races.get(races.size() - 1).getId();
                predictionViewModel.predictFullRace(lastRaceId);
            }
        });

        dashboardViewModel.errorLiveData.observe(this, error -> {
            if (error != null) Toast.makeText(this, "Courses : " + error, Toast.LENGTH_SHORT).show();
        });

        // Quand les prédictions arrivent, mettre à jour l'adaptateur
        predictionViewModel.standingsLiveData.observe(this, predictions -> {
            if (predictions != null) {
                adapter.updateData(predictionsToDrivers(predictions));
            }
        });

        predictionViewModel.errorLiveData.observe(this, error -> {
            if (error != null) Toast.makeText(this, "Prédictions : " + error, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Convertit une liste de Prediction en liste de Driver ordonnée par position prédite.
     * Le champ Prediction.driver est une chaîne "Prénom Nom" fournie par l'API.
     */
    private List<Driver> predictionsToDrivers(List<Prediction> predictions) {
        List<Prediction> sorted = new ArrayList<>(predictions);
        sorted.sort((a, b) -> Integer.compare(a.getPredictedPosition(), b.getPredictedPosition()));

        List<Driver> drivers = new ArrayList<>();
        for (Prediction p : sorted) {
            Driver d = new Driver();
            // L'API retourne le nom complet dans le champ "driver"
            String[] parts = p.getDriver() != null ? p.getDriver().split(" ", 2) : new String[]{"?", ""};
            d.setFirstName(parts[0]);
            d.setLastName(parts.length > 1 ? parts[1] : "");
            d.setPodiumProbability(p.getPodiumProbability());
            drivers.add(d);
        }
        return drivers;
    }

    // ── Ouvrir le BottomSheet pilote ─────────────────────────────────
    private void openDriverSheet(Driver driver, int position) {
        DriverBottomSheet sheet = DriverBottomSheet.newInstance(driver, position);
        sheet.show(getSupportFragmentManager(), DriverBottomSheet.class.getSimpleName());
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

        ((ImageView) findViewById(R.id.iconPodium)).setColorFilter(activeColor);
        ((TextView)  findViewById(R.id.labelPodium)).setTextColor(activeColor);

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                if (tab[0] == R.id.tabPodium) return;

                for (int[] t : tabs) {
                    ((ImageView) findViewById(t[1])).setColorFilter(inactiveColor);
                    ((TextView)  findViewById(t[2])).setTextColor(inactiveColor);
                }
                ((ImageView) findViewById(tab[1])).setColorFilter(activeColor);
                ((TextView)  findViewById(tab[2])).setTextColor(activeColor);

                Class<?> target = null;
                if (tab[0] == R.id.tabGlobe)   target = DashboardActivity.class;
                if (tab[0] == R.id.tabPredict)  target = PredictionActivity.class;
                if (tab[0] == R.id.tabDriver)   target = ProfileActivity.class;

                if (target != null) {
                    startActivity(new Intent(this, target));
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }
}
