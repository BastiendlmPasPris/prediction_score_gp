package com.example.prediction_score_gp.ui.standings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class StandingsActivity extends AppCompatActivity {

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
        setupNavBar();
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

        rvStandings.setLayoutManager(new LinearLayoutManager(this));
        rvStandings.setAdapter(new StandingsAdapter(
                getSampleData(),
                this::openDriverSheet   // clic → bottom sheet
        ));
    }

    // ── Données de test ──────────────────────────────────────────────
    // TODO : remplacer par un appel au ViewModel / Repository
    private List<Driver> getSampleData() {
        List<Driver> list = new ArrayList<>();

        Driver d1 = new Driver();
        d1.setFirstName("Lando"); d1.setLastName("Norris");
        d1.setTeam("McLaren");    d1.setPodiumProbability(0.92);
        list.add(d1);

        Driver d2 = new Driver();
        d2.setFirstName("Max");   d2.setLastName("Verstappen");
        d2.setTeam("Red Bull");   d2.setPodiumProbability(0.85);
        list.add(d2);

        Driver d3 = new Driver();
        d3.setFirstName("Oscar"); d3.setLastName("Piastri");
        d3.setTeam("McLaren");    d3.setPodiumProbability(0.78);
        list.add(d3);

        Driver d4 = new Driver();
        d4.setFirstName("George"); d4.setLastName("Russell");
        d4.setTeam("Mercedes");    d4.setPodiumProbability(0.60);
        list.add(d4);

        Driver d5 = new Driver();
        d5.setFirstName("Charles"); d5.setLastName("Leclerc");
        d5.setTeam("Ferrari");      d5.setPodiumProbability(0.55); d5.setNationality("Monaco");
        list.add(d5);

        Driver d6 = new Driver();
        d6.setFirstName("Lewis"); d6.setLastName("Hamilton");
        d6.setTeam("Mercedes");   d6.setPodiumProbability(0.40);
        list.add(d6);

        return list;
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

        // Onglet Podium actif par défaut (écran courant)
        ((ImageView) findViewById(R.id.iconPodium)).setColorFilter(activeColor);
        ((TextView)  findViewById(R.id.labelPodium)).setTextColor(activeColor);

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                if (tab[0] == R.id.tabPodium) return; // déjà sur cet écran

                // Reset visuel
                for (int[] t : tabs) {
                    ((ImageView) findViewById(t[1])).setColorFilter(inactiveColor);
                    ((TextView)  findViewById(t[2])).setTextColor(inactiveColor);
                }
                ((ImageView) findViewById(tab[1])).setColorFilter(activeColor);
                ((TextView)  findViewById(tab[2])).setTextColor(activeColor);

                // Navigation
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