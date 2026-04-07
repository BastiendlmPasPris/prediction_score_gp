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

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.local.SessionManager;
import com.example.prediction_score_gp.data.model.User;
import com.example.prediction_score_gp.ui.BaseActivity;
import com.example.prediction_score_gp.ui.auth.LoginActivity;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;
import com.example.prediction_score_gp.util.HapticHelper;
import com.example.prediction_score_gp.util.LocaleHelper;
import com.example.prediction_score_gp.util.SwipeNavigationHelper;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends BaseActivity {

    private TextView tvUsername, tvEmail, tvRole;
    private PredictionViewModel predictionViewModel;
    private HistoryAdapter historyAdapter;

    // Widgets paramètres
    private MaterialButton btnLangEn, btnLangFr;
    private TextInputEditText etApiUrl;

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
        if (btnLogout != null) btnLogout.setOnClickListener(v -> {
            HapticHelper.tap(v);
            logoutUser();
        });

        loadUserProfile();
        setupHistoryRecyclerView();
        setupLanguageToggle();
        setupApiUrlSettings();
        setupNavBar();
        setupSwipeNavigation();

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

    // ── Sélecteur de langue ──────────────────────────────────────────
    private void setupLanguageToggle() {
        btnLangEn = findViewById(R.id.btnLangEn);
        btnLangFr = findViewById(R.id.btnLangFr);
        if (btnLangEn == null || btnLangFr == null) return;

        // Afficher la sélection courante
        String currentLang = LocaleHelper.getLanguage(this);
        updateLangButtons(currentLang);

        btnLangEn.setOnClickListener(v -> {
            HapticHelper.confirm(v);
            LocaleHelper.setLanguage(this, LocaleHelper.LANG_EN);
            recreate(); // recharge l'Activity avec la nouvelle locale
        });

        btnLangFr.setOnClickListener(v -> {
            HapticHelper.confirm(v);
            LocaleHelper.setLanguage(this, LocaleHelper.LANG_FR);
            recreate();
        });
    }

    private void updateLangButtons(String activeLang) {
        if (btnLangEn == null || btnLangFr == null) return;
        boolean isEn = LocaleHelper.LANG_EN.equals(activeLang);

        btnLangEn.setBackgroundTintList(
                isEn ? android.content.res.ColorStateList.valueOf(Color.parseColor("#FF3030"))
                     : android.content.res.ColorStateList.valueOf(Color.parseColor("#1A1A1A")));
        btnLangEn.setTextColor(isEn ? Color.WHITE : Color.parseColor("#888888"));

        btnLangFr.setBackgroundTintList(
                !isEn ? android.content.res.ColorStateList.valueOf(Color.parseColor("#FF3030"))
                      : android.content.res.ColorStateList.valueOf(Color.parseColor("#1A1A1A")));
        btnLangFr.setTextColor(!isEn ? Color.WHITE : Color.parseColor("#888888"));
    }

    // ── URL API configurable ─────────────────────────────────────────
    private void setupApiUrlSettings() {
        etApiUrl = findViewById(R.id.etApiUrl);
        MaterialButton btnSaveUrl = findViewById(R.id.btnSaveUrl);
        if (etApiUrl == null || btnSaveUrl == null) return;

        // Pré-remplir avec l'URL actuelle
        etApiUrl.setText(SessionManager.getApiUrl(this));

        btnSaveUrl.setOnClickListener(v -> {
            HapticHelper.confirm(v);
            String url = etApiUrl.getText() != null
                    ? etApiUrl.getText().toString().trim()
                    : "";

            if (url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                Toast.makeText(this, getString(R.string.msg_url_invalid), Toast.LENGTH_SHORT).show();
                return;
            }
            SessionManager.setApiUrl(this, url);
            Toast.makeText(this, getString(R.string.msg_url_saved), Toast.LENGTH_LONG).show();
        });
    }

    private void logoutUser() {
        SessionManager.clear(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Navigation par swipe ──────────────────────────────────────────
    private void setupSwipeNavigation() {
        View rootLayout = findViewById(R.id.rootLayout);
        SwipeNavigationHelper.attach(rootLayout, new SwipeNavigationHelper.OnSwipeCallback() {
            @Override
            public void onSwipeLeft() {  // pas d'onglet suivant après Driver
                HapticHelper.tap(rootLayout);
            }

            @Override
            public void onSwipeRight() { // → Podium (onglet précédent)
                HapticHelper.tap(rootLayout);
                startActivity(new Intent(ProfileActivity.this, StandingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
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
                HapticHelper.tap(v);
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
