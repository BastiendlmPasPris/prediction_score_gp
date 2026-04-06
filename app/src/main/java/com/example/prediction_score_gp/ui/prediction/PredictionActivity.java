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
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;
import com.example.prediction_score_gp.viewmodel.RaceViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PredictionActivity extends AppCompatActivity {

    private PredictionViewModel predictionViewModel;
    private RaceViewModel raceViewModel;
    private RaceAdapter raceAdapter;

    // --- Stockage en mémoire pour l'ouverture automatique ---
    private List<Driver> availableDrivers = new ArrayList<>();
    private List<Race> availableRaces = new ArrayList<>();
    private int autoOpenRaceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_prediction);

        // On récupère l'ID envoyé par le Globe
        autoOpenRaceId = getIntent().getIntExtra("SELECTED_RACE_ID", -1);

        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        raceViewModel = new ViewModelProvider(this).get(RaceViewModel.class);

        setupWindowInsets();
        setupRecyclerView();
        setupNavBar();
        setupObservers();

        // Lancement des requêtes simultanées
        raceViewModel.loadRaces(2026);
        predictionViewModel.loadDrivers();
    }

    private void setupObservers() {
        predictionViewModel.predictionLiveData.observe(this, prediction -> {
            if (prediction != null) {
                String resultat = "🏁 Position estimée : " + prediction.getPredictedPosition() +
                        "\n🏆 Probabilité Podium : " + String.format("%.1f", prediction.getPodiumProbability() * 100) + "%";
                Toast.makeText(this, resultat, Toast.LENGTH_LONG).show();
            }
        });

        predictionViewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Erreur : " + error, Toast.LENGTH_LONG).show();
            }
        });

        predictionViewModel.loadingLiveData.observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                Toast.makeText(this, "L'IA analyse les données...", Toast.LENGTH_SHORT).show();
            }
        });

        // 1. Quand les PILOTES arrivent
        predictionViewModel.driversLiveData.observe(this, drivers -> {
            if (drivers != null) {
                this.availableDrivers = drivers;
                tryAutoOpenSheet(); // On vérifie si on peut ouvrir le BottomSheet
            }
        });

        // 2. Quand les COURSES arrivent
        raceViewModel.racesLiveData.observe(this, allRaces -> {
            if (allRaces != null) {
                this.availableRaces = allRaces.stream()
                        .filter(r -> r.getSeason() == 2026)
                        .collect(Collectors.toList());

                raceAdapter.updateRaces(this.availableRaces);
                tryAutoOpenSheet(); // On vérifie si on peut ouvrir le BottomSheet
            }
        });
    }

    // Garantit que le BottomSheet ne s'ouvre que si prêt
    private void tryAutoOpenSheet() {
        // Si on a bien reçu une demande du globe (!= -1) ET que les deux listes API sont chargées
        if (autoOpenRaceId != -1 && !availableDrivers.isEmpty() && !availableRaces.isEmpty()) {
            for (Race r : availableRaces) {
                if (r.getId() == autoOpenRaceId) {
                    openRaceSheet(r); // On ouvre enfin la feuille !
                    autoOpenRaceId = -1; // On détruit la demande pour ne pas la rouvrir en boucle
                    break;
                }
            }
        }
    }

    private void setupWindowInsets() {
        View rootLayout = findViewById(R.id.rootLayout);
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

    private void setupRecyclerView() {
        RecyclerView rvRaces = findViewById(R.id.rvRaces);
        if (rvRaces == null) return;
        rvRaces.setLayoutManager(new LinearLayoutManager(this));
        raceAdapter = new RaceAdapter(new ArrayList<>(), this::openRaceSheet);
        rvRaces.setAdapter(raceAdapter);
    }

    private void openRaceSheet(Race race) {
        RaceBottomSheet sheet = RaceBottomSheet.newInstance(race);
        sheet.setDrivers(availableDrivers);

        sheet.setOnPredictListener((selectedRace, driverId) -> {
            predictionViewModel.predict(selectedRace.getId(), driverId);
        });

        sheet.show(getSupportFragmentManager(), RaceBottomSheet.class.getSimpleName());
    }

    private void setupNavBar() {
        int activeColor = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        int[][] tabs = {
                {R.id.tabGlobe, R.id.iconGlobe, R.id.labelGlobe},
                {R.id.tabPredict, R.id.iconPredict, R.id.labelPredict},
                {R.id.tabPodium, R.id.iconPodium, R.id.labelPodium},
                {R.id.tabDriver, R.id.iconDriver, R.id.labelDriver},
        };

        ((ImageView) findViewById(R.id.iconPredict)).setColorFilter(activeColor);
        ((TextView) findViewById(R.id.labelPredict)).setTextColor(activeColor);

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                if (tab[0] == R.id.tabPredict) return;

                for (int[] t : tabs) {
                    ImageView img = findViewById(t[1]);
                    TextView txt = findViewById(t[2]);
                    if (img != null) img.setColorFilter(inactiveColor);
                    if (txt != null) txt.setTextColor(inactiveColor);
                }
                ((ImageView) findViewById(tab[1])).setColorFilter(activeColor);
                ((TextView) findViewById(tab[2])).setTextColor(activeColor);

                Class<?> target = null;
                if (tab[0] == R.id.tabGlobe) target = DashboardActivity.class;
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