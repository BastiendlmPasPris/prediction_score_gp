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
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;

import java.util.ArrayList;
import com.example.prediction_score_gp.viewmodel.RaceViewModel;
import java.util.stream.Collectors;
import java.util.List;

public class PredictionActivity extends AppCompatActivity {

    private PredictionViewModel predictionViewModel;
    private RaceAdapter raceAdapter;
    private RecyclerView rvRaces;
    private RaceViewModel raceViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_prediction);

        // 1. Initialiser les ViewModels
        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        raceViewModel = new ViewModelProvider(this).get(RaceViewModel.class); // <--- AJOUTÉ

        setupWindowInsets();
        setupRecyclerView();
        setupNavBar();
        setupObservers();

        // 2. Lancer le chargement des courses pour 2026
        raceViewModel.loadRaces(2026);
    }

    // ── Observers API ────────────────────────────────────────────────
    private void setupObservers() {
        // Écouter quand la prédiction ML arrive
        predictionViewModel.predictionLiveData.observe(this, prediction -> {
            if (prediction != null) {
                String resultat = "🏁 Position estimée : " + prediction.getPredictedPosition() +
                        "\n🏆 Probabilité Podium : " + String.format("%.1f", prediction.getPodiumProbability() * 100) + "%";
                Toast.makeText(this, resultat, Toast.LENGTH_LONG).show();

                // L'idéal ici sera de mettre à jour le BottomSheet s'il est encore ouvert
            }
        });

        // Écouter les erreurs
        predictionViewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Erreur : " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Écouter le temps de chargement
        predictionViewModel.loadingLiveData.observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                Toast.makeText(this, "L'IA analyse les données...", Toast.LENGTH_SHORT).show();
            }
        });

        raceViewModel.racesLiveData.observe(this, allRaces -> {
            if (allRaces != null) {
                // On filtre pour être sûr de n'avoir que 2026 (même si l'API le fait déjà)
                List<Race> races2026 = allRaces.stream()
                        .filter(r -> r.getSeason() == 2026)
                        .collect(Collectors.toList());

                // Mettre à jour l'adapter avec les vraies données
                raceAdapter.updateRaces(races2026);
            }
        });
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

        rvRaces.setLayoutManager(new LinearLayoutManager(this));

        // On initialise avec une liste VIDE (On retire les données de test)
        // Quand l'API répondra avec les courses, il faudra mettre à jour cet adapter
        raceAdapter = new RaceAdapter(new ArrayList<>(), this::openRaceSheet);
        rvRaces.setAdapter(raceAdapter);
    }

    // ── Ouvrir le BottomSheet ────────────────────────────────────────
    private void openRaceSheet(Race race) {
        RaceBottomSheet sheet = RaceBottomSheet.newInstance(race);

        // ATTENTION : Pour que l'API fonctionne, le listener doit renvoyer l'ID du pilote (un int)
        // et non pas son nom (String).
        sheet.setOnPredictListener((selectedRace, driverId) -> {
            // On envoie la requête à l'API via le ViewModel !
            predictionViewModel.predict(selectedRace.getId(), driverId);
        });

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