package com.example.prediction_score_gp.ui.standings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.ui.BaseActivity;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.util.HapticHelper;
import com.example.prediction_score_gp.util.SwipeNavigationHelper;
import com.example.prediction_score_gp.viewmodel.DashboardViewModel;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;

import java.util.ArrayList;
import java.util.List;

public class StandingsActivity extends BaseActivity {

    private StandingsAdapter adapter;
    private PredictionViewModel predictionViewModel;
    private DashboardViewModel dashboardViewModel;
    private SwipeRefreshLayout swipeRefresh;

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
        setupSwipeRefresh();
        setupNavBar();
        setupSwipeNavigation();

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

    // ── SwipeRefreshLayout ───────────────────────────────────────────
    private void setupSwipeRefresh() {
        swipeRefresh = findViewById(R.id.swipeRefreshStandings);
        if (swipeRefresh == null) return;
        swipeRefresh.setColorSchemeColors(Color.parseColor("#FF3030"));
        swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#1A1A1A"));
        swipeRefresh.setOnRefreshListener(() -> {
            HapticHelper.tap(swipeRefresh);
            dashboardViewModel.loadRaces(2024);
        });
    }

    // ── ViewModels ────────────────────────────────────────────────────
    private void setupViewModels() {
        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        dashboardViewModel  = new ViewModelProvider(this).get(DashboardViewModel.class);

        dashboardViewModel.racesLiveData.observe(this, races -> {
            if (races != null && !races.isEmpty()) {
                int lastRaceId = races.get(races.size() - 1).getId();
                predictionViewModel.predictFullRace(lastRaceId);
            }
        });

        dashboardViewModel.errorLiveData.observe(this, error -> {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (error != null) Toast.makeText(this,
                    getString(R.string.error_races_prefix) + error, Toast.LENGTH_SHORT).show();
        });

        predictionViewModel.standingsLiveData.observe(this, predictions -> {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (predictions != null) {
                adapter.updateData(predictionsToDrivers(predictions));
            }
        });

        predictionViewModel.errorLiveData.observe(this, error -> {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (error != null) Toast.makeText(this,
                    getString(R.string.error_predictions_prefix) + error, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Convertit une liste de Prediction en liste de Driver ordonnée par position prédite.
     */
    private List<Driver> predictionsToDrivers(List<Prediction> predictions) {
        List<Prediction> sorted = new ArrayList<>(predictions);
        sorted.sort((a, b) -> Integer.compare(a.getPredictedPosition(), b.getPredictedPosition()));

        List<Driver> drivers = new ArrayList<>();
        for (Prediction p : sorted) {
            Driver d = new Driver();
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
        HapticHelper.tap(findViewById(R.id.rootLayout));
        DriverBottomSheet sheet = DriverBottomSheet.newInstance(driver, position);
        sheet.show(getSupportFragmentManager(), DriverBottomSheet.class.getSimpleName());
    }

    // ── Navigation par swipe ──────────────────────────────────────────
    private void setupSwipeNavigation() {
        View rootLayout = findViewById(R.id.rootLayout);
        SwipeNavigationHelper.attach(rootLayout, new SwipeNavigationHelper.OnSwipeCallback() {
            @Override
            public void onSwipeLeft() {  // → Profile (onglet suivant)
                HapticHelper.tap(rootLayout);
                startActivity(new Intent(StandingsActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }

            @Override
            public void onSwipeRight() { // → Predict (onglet précédent)
                HapticHelper.tap(rootLayout);
                startActivity(new Intent(StandingsActivity.this, PredictionActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
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
                HapticHelper.tap(v);
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
