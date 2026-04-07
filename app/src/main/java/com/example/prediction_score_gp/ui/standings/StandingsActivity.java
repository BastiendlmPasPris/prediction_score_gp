package com.example.prediction_score_gp.ui.standings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.prediction.RaceBottomSheet;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;
import com.example.prediction_score_gp.viewmodel.RaceViewModel;

// Assure-toi que cet import correspond au bon package de ton application !
// import com.example.prediction_score_gp.ui.standings.DriverBottomSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StandingsActivity extends BaseActivity {

    private StandingsAdapter adapter;
    private PredictionViewModel predictionViewModel;
    private DashboardViewModel dashboardViewModel;
    private SwipeRefreshLayout swipeRefresh;

    private PredictionViewModel predictionViewModel;
    private RaceViewModel raceViewModel;
    private StandingsAdapter adapter;

    private List<Driver> allDrivers = new ArrayList<>();
    private Spinner spinnerRace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_standings);

        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        raceViewModel = new ViewModelProvider(this).get(RaceViewModel.class);

        setupWindowInsets();
        setupRecyclerView();
        setupNavBar();

        spinnerRace = findViewById(R.id.spinnerRace);

        predictionViewModel.loadDrivers();
        raceViewModel.loadRaces(2026);

        setupObservers();
    }

    private void setupObservers() {
        predictionViewModel.driversLiveData.observe(this, drivers -> {
            if (drivers != null) allDrivers = drivers;
        });

        raceViewModel.racesLiveData.observe(this, races -> {
            if (races != null && !races.isEmpty()) {
                List<Race> races2026 = races.stream().filter(r -> r.getSeason() == 2026).collect(Collectors.toList());
                setupRaceSpinner(races2026);
            }
        });

        predictionViewModel.standingsLiveData.observe(this, rawPredictions -> {
            if (rawPredictions != null && !allDrivers.isEmpty()) {
                updatePodium(rawPredictions);
            }
        });

        predictionViewModel.loadingLiveData.observe(this, isLoading -> {
            if (isLoading) Toast.makeText(this, "L'IA simule la course...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRaceSpinner(List<Race> races) {
        List<RaceOption> options = new ArrayList<>();
        options.add(new RaceOption(-1, "— Sélectionner un Grand Prix —"));
        for (Race r : races) {
            String flag = RaceBottomSheet.getFlagEmoji(r.getCountry());
            options.add(new RaceOption(r.getId(), flag + " " + r.getName()));
        }

        ArrayAdapter<RaceOption> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.parseColor("#E0E0E0"));
                tv.setPadding(32, 16, 32, 16);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#1A1A1A"));
                tv.setPadding(32, 32, 32, 32);
                return tv;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRace.setAdapter(spinnerAdapter);

        spinnerRace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RaceOption selected = (RaceOption) parent.getItemAtPosition(position);
                if (selected.id != -1) predictionViewModel.predictFullRace(selected.id);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePodium(List<Prediction> rawPredictions) {
        List<Integer> grid2026Ids = Arrays.asList(830, 1, 844, 846, 857, 847, 832, 4, 840, 815, 848, 866, 839, 822, 842, 807, 860,863, 872, 859, 861, 876);


        List<Prediction> predictions = new ArrayList<>();
        for (Prediction p : rawPredictions) {
            Driver d = findDriverByName(p.getDriver());
            if (d != null && grid2026Ids.contains(d.getId())) {
                predictions.add(p);
            }
            if (predictions.size() == 22) break;
        }

        if (predictions.size() < 3) return;

        // --- AFFICHAGE ET CLICS DU TOP 3 ---
        Driver p1 = findDriverByName(predictions.get(0).getDriver());
        Driver p2 = findDriverByName(predictions.get(1).getDriver());
        Driver p3 = findDriverByName(predictions.get(2).getDriver());

        if (p1 != null) {
            ((TextView) findViewById(R.id.tvP1Name)).setText(p1.getLastName());
            findViewById(R.id.colorP1).setBackgroundColor(getTeamColor(p1.getTeam()));
            findViewById(R.id.cardP1).setOnClickListener(v -> openDriverSheet(p1, predictions.get(0).getPodiumProbability(), 1));
        }

        if (p2 != null) {
            ((TextView) findViewById(R.id.tvP2Name)).setText(p2.getLastName());
            findViewById(R.id.colorP2).setBackgroundColor(getTeamColor(p2.getTeam()));
            findViewById(R.id.cardP2).setOnClickListener(v -> openDriverSheet(p2, predictions.get(1).getPodiumProbability(), 2));
        }

        if (p3 != null) {
            ((TextView) findViewById(R.id.tvP3Name)).setText(p3.getLastName());
            findViewById(R.id.colorP3).setBackgroundColor(getTeamColor(p3.getTeam()));
            findViewById(R.id.cardP3).setOnClickListener(v -> openDriverSheet(p3, predictions.get(2).getPodiumProbability(), 3));
        }

        if (predictions.size() > 3) {
            List<Prediction> restOfGrid = predictions.subList(3, predictions.size());
            adapter.updateData(restOfGrid, allDrivers);
        }
    }

    private void openDriverSheet(Driver driver, double proba, int position) {
        DriverBottomSheet sheet = DriverBottomSheet.newInstance(driver, proba, position);
        sheet.show(getSupportFragmentManager(), DriverBottomSheet.class.getSimpleName());
    }

    private Driver findDriverByName(String predictedName) {
        if (predictedName == null || predictedName.isEmpty()) return null;
        String lowerPredictedName = predictedName.toLowerCase();
        for (Driver d : allDrivers) {
            if (d.getLastName() != null && lowerPredictedName.contains(d.getLastName().toLowerCase())) {
                return d;
            }
        }
        return null;
    }

    public static int getTeamColor(String team) {
        if (team == null) return Color.GRAY;
        String t = team.toLowerCase();
        if (t.contains("ferrari")) return Color.parseColor("#E8002D");
        if (t.contains("red bull")) return Color.parseColor("#3671C6");
        if (t.contains("mclaren")) return Color.parseColor("#FF8000");
        if (t.contains("mercedes")) return Color.parseColor("#27F4D2");
        if (t.contains("aston")) return Color.parseColor("#229971");
        if (t.contains("alpine")) return Color.parseColor("#FF87BC");
        if (t.contains("williams")) return Color.parseColor("#005AFF");
        if (t.contains("haas")) return Color.parseColor("#B6BABD");
        if (t.contains("sauber") || t.contains("kick")) return Color.parseColor("#52E252");
        if (t.contains("rb") || t.contains("toro")) return Color.parseColor("#6692FF");
        return Color.GRAY;
    }

    private void setupRecyclerView() {
        RecyclerView rvStandings = findViewById(R.id.rvStandings);
        rvStandings.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StandingsAdapter(this::openDriverSheet);
        rvStandings.setAdapter(adapter);
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

    private void setupNavBar() {
        int activeColor = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");
        int[][] tabs = {
                {R.id.tabGlobe, R.id.iconGlobe, R.id.labelGlobe},
                {R.id.tabPredict, R.id.iconPredict, R.id.labelPredict},
                {R.id.tabPodium, R.id.iconPodium, R.id.labelPodium},
                {R.id.tabDriver, R.id.iconDriver, R.id.labelDriver},
        };
        ((ImageView) findViewById(R.id.iconPodium)).setColorFilter(activeColor);
        ((TextView) findViewById(R.id.labelPodium)).setTextColor(activeColor);
        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;
            tabView.setOnClickListener(v -> {
                if (tab[0] == R.id.tabPodium) return;
                Class<?> target = null;
                if (tab[0] == R.id.tabGlobe) target = DashboardActivity.class;
                if (tab[0] == R.id.tabPredict) target = PredictionActivity.class;
                if (tab[0] == R.id.tabDriver) target = ProfileActivity.class;
                if (target != null) {
                    startActivity(new Intent(this, target));
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }

    private static class RaceOption {
        int id; String name;
        RaceOption(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}