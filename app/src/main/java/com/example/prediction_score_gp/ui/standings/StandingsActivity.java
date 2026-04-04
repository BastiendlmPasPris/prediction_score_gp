package com.example.prediction_score_gp.ui.standings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.viewmodel.StandingsViewModel;

import java.util.ArrayList;
import java.util.List;

public class StandingsActivity extends AppCompatActivity {

    private StandingsViewModel viewModel;
    private StandingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Plein écran
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_standings);

        setupWindowInsets();
        setupRecyclerView();
        setupNavBar();
        setupViewModel();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StandingsViewModel.class);

        // Observer les prédictions
        viewModel.predictedStandings.observe(this, drivers -> {
            if (drivers != null) {
                adapter.updateData(drivers);
            }
        });

        viewModel.raceName.observe(this, name -> {
            TextView tvTitle = findViewById(R.id.tvSubtitle);
            if (tvTitle != null && name != null) {
                tvTitle.setText("Prédiction : " + name);
            }
        });

        // Lancer la prédiction pour le premier GP de 2026 (Australie)
        viewModel.loadPredictions(1169);
    }

    private void setupRecyclerView() {
        RecyclerView rvStandings = findViewById(R.id.rvStandings);
        if (rvStandings == null) return;

        rvStandings.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation avec une liste vide
        adapter = new StandingsAdapter(new ArrayList<>(), (driver, pos) -> {
            DriverBottomSheet bottomSheet = DriverBottomSheet.newInstance(driver, pos);

            // On l'affiche par-dessus l'écran
            bottomSheet.show(getSupportFragmentManager(), "DriverBottomSheet");
        });
        rvStandings.setAdapter(adapter);
    }

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

    private void setupNavBar() {
        int activeColor   = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        // Onglet Podium actif
        ((ImageView) findViewById(R.id.iconPodium)).setColorFilter(activeColor);
        ((TextView)  findViewById(R.id.labelPodium)).setTextColor(activeColor);

        // Navigation
        findViewById(R.id.tabGlobe).setOnClickListener(v -> navigateTo(DashboardActivity.class));
        findViewById(R.id.tabPredict).setOnClickListener(v -> navigateTo(PredictionActivity.class));
        findViewById(R.id.tabDriver).setOnClickListener(v -> navigateTo(ProfileActivity.class));
    }

    private void navigateTo(Class<?> target) {
        startActivity(new Intent(this, target));
        overridePendingTransition(0, 0);
        finish();
    }
}