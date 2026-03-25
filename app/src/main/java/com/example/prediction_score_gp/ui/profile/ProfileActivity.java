package com.example.prediction_score_gp.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.User;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mode plein écran
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_profile);

        // 1. Gestion des barres système (Insets)
        setupWindowInsets();

        // 2. Initialisation des Vues
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);

        findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());

        // 3. Charger les données (Simulation)
        loadUserProfile();
        setupHistoryRecyclerView();

        // 4. Configuration de la NavBar
        setupNavBar();
    }

    private void setupWindowInsets() {
        View rootLayout = findViewById(R.id.rootLayout);
        View statusSpacer = findViewById(R.id.statusBarSpacer);
        View navBarSpacer = findViewById(R.id.navBarSpacer);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (statusSpacer != null) {
                ViewGroup.LayoutParams spTop = statusSpacer.getLayoutParams();
                spTop.height = sysBars.top;
                statusSpacer.setLayoutParams(spTop);
            }
            if (navBarSpacer != null) {
                ViewGroup.LayoutParams spBot = navBarSpacer.getLayoutParams();
                spBot.height = sysBars.bottom;
                navBarSpacer.setLayoutParams(spBot);
            }
            return insets;
        });
    }

    private void loadUserProfile() {
        // TODO: Remplacer par l'appel à SharedPreferences ou ton ViewModel
        User mockUser = new User(1, "max.verstappen@f1.com", "SuperMax", "user", "dummy_token");

        if (tvUsername != null) tvUsername.setText(mockUser.getUsername());
        if (tvEmail != null) tvEmail.setText(mockUser.getEmail());
        if (tvRole != null) tvRole.setText(mockUser.getRole().toUpperCase());
    }

    private void setupHistoryRecyclerView() {
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            // TODO: Créer un HistoryAdapter et l'attacher ici
            // rvHistory.setAdapter(new HistoryAdapter(votreListe));
        }
    }

    private void logoutUser() {
        // TODO: Effacer le token JWT des SharedPreferences
        Toast.makeText(this, "Déconnexion en cours...", Toast.LENGTH_SHORT).show();

        // Redirection vers l'écran de login (à adapter selon ta structure)
        // Intent intent = new Intent(this, LoginActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();
    }

    private void setupNavBar() {
        int[][] tabs = {
                {R.id.tabGlobe, R.id.iconGlobe, R.id.labelGlobe},
                {R.id.tabPredict, R.id.iconPredict, R.id.labelPredict},
                {R.id.tabPodium, R.id.iconPodium, R.id.labelPodium},
                {R.id.tabDriver, R.id.iconDriver, R.id.labelDriver},
        };

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                // Si on clique sur l'onglet déjà actif (Driver/Profile), on ne fait rien
                if (tab[0] == R.id.tabDriver) return;

                Intent intent = null;
                if (tab[0] == R.id.tabGlobe) {
                    intent = new Intent(this, DashboardActivity.class);
                } else if (tab[0] == R.id.tabPredict) {
                    intent = new Intent(this, PredictionActivity.class);
                } else if (tab[0] == R.id.tabPodium) {
                    intent = new Intent(this, StandingsActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }
}