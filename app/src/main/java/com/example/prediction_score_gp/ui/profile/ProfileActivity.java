package com.example.prediction_score_gp.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvRole;
    private View userCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mode plein écran pour l'immersion
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_profile);

        // 1. Initialisation des Vues
        userCard = findViewById(R.id.userCard);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);

        // 2. Configuration des interactions
        findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());

        // 3. Setup des composants
        setupWindowInsets();
        loadUserData();
        setupHistoryList();
        setupNavBar();

        // 4. Petite animation d'apparition
        userCard.setAlpha(0f);
        userCard.setTranslationY(20f);
        userCard.animate().alpha(1f).translationY(0f).setDuration(500).start();
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

    private void loadUserData() {
        // On récupère le fichier de sauvegarde nommé "UserPrefs"
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // On extrait les valeurs. Si elles n'existent pas, on met une valeur par défaut ("—")
        String username = prefs.getString("username", "Guest");
        String email    = prefs.getString("email", "not_connected@f1.com");
        String role     = prefs.getString("role", "MEMBER");

        // Mise à jour de l'interface
        tvUsername.setText(username);
        tvEmail.setText(email);
        tvRole.setText(role.toUpperCase());

        // Optionnel : Changer la couleur du badge selon le rôle
        if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("analyst")) {
            tvRole.setTextColor(Color.parseColor("#FFD700")); // Or pour les VIP
            tvRole.setBackgroundColor(Color.parseColor("#26FFD700"));
        }
    }

    private void setupHistoryList() {
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            // Ici tu pourras attacher ton HistoryAdapter quand il sera prêt
            // rvHistory.setAdapter(new HistoryAdapter(data));
        }
    }

    private void logoutUser() {

        Toast.makeText(this, "Déconnexion en cours...", Toast.LENGTH_SHORT).show();

        // Redirection vers Login (à décommenter quand tu auras LoginActivity)
        /*
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        */
    }

    private void setupNavBar() {
        int activeColor = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        int[][] tabs = {
                {R.id.tabGlobe,   R.id.iconGlobe,   R.id.labelGlobe},
                {R.id.tabPredict, R.id.iconPredict,  R.id.labelPredict},
                {R.id.tabPodium,  R.id.iconPodium,   R.id.labelPodium},
                {R.id.tabDriver,  R.id.iconDriver,   R.id.labelDriver},
        };

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                // On ne fait rien si on est déjà sur la page Driver
                if (tab[0] == R.id.tabDriver) return;

                Class<?> target = null;
                if (tab[0] == R.id.tabGlobe) target = DashboardActivity.class;
                else if (tab[0] == R.id.tabPredict) target = PredictionActivity.class;
                else if (tab[0] == R.id.tabPodium) target = StandingsActivity.class;

                if (target != null) {
                    Intent intent = new Intent(this, target);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Transition fluide pour la nav bar
                    finish();
                }
            });
        }
    }
}