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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.local.SessionManager;
import com.example.prediction_score_gp.data.model.User;
import com.example.prediction_score_gp.ui.auth.LoginActivity;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvRole;
    private PredictionViewModel predictionViewModel;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_profile);

        setupWindowInsets();

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail    = findViewById(R.id.tvEmail);
        tvRole     = findViewById(R.id.tvRole);

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> logoutUser());

        loadUserProfile();
        setupHistoryRecyclerView();
        setupNavBar();

        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        predictionViewModel.standingsLiveData.observe(this, predictions -> {
            if (predictions != null && historyAdapter != null) {
                historyAdapter.updateData(predictions);
            }
        });
        predictionViewModel.errorLiveData.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupWindowInsets() {
        View rootLayout   = findViewById(R.id.rootLayout);
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
        User user = SessionManager.getUser(this);
        if (user == null) return;

        if (tvUsername != null) tvUsername.setText(user.getUsername());
        if (tvEmail    != null) tvEmail.setText(user.getEmail());
        if (tvRole     != null) tvRole.setText(user.getRole().toUpperCase());
    }

    private void setupHistoryRecyclerView() {
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        if (rvHistory == null) return;

        historyAdapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void logoutUser() {
        SessionManager.clear(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavBar() {
        int activeColor   = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        int[][] tabs = {
                {R.id.tabGlobe,   R.id.iconGlobe,   R.id.labelGlobe},
                {R.id.tabPredict, R.id.iconPredict,  R.id.labelPredict},
                {R.id.tabPodium,  R.id.iconPodium,   R.id.labelPodium},
                {R.id.tabDriver,  R.id.iconDriver,   R.id.labelDriver},
        };

        ImageView iconDriver = findViewById(R.id.iconDriver);
        TextView  labelDriver = findViewById(R.id.labelDriver);
        if (iconDriver  != null) iconDriver.setColorFilter(activeColor);
        if (labelDriver != null) labelDriver.setTextColor(activeColor);

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                if (tab[0] == R.id.tabDriver) return;

                for (int[] t : tabs) {
                    ImageView img = findViewById(t[1]);
                    TextView  txt = findViewById(t[2]);
                    if (img != null) img.setColorFilter(inactiveColor);
                    if (txt != null) txt.setTextColor(inactiveColor);
                }
                ((ImageView) findViewById(tab[1])).setColorFilter(activeColor);
                ((TextView)  findViewById(tab[2])).setTextColor(activeColor);

                Intent intent = null;
                if (tab[0] == R.id.tabGlobe)   intent = new Intent(this, DashboardActivity.class);
                if (tab[0] == R.id.tabPredict)  intent = new Intent(this, PredictionActivity.class);
                if (tab[0] == R.id.tabPodium)   intent = new Intent(this, StandingsActivity.class);

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            });
        }
    }
}
