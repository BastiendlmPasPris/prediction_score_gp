package com.example.prediction_score_gp.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.prediction.RaceBottomSheet;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;
import com.example.prediction_score_gp.viewmodel.DashboardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private WebView globeWebView;
    private MaterialCardView cityCard;
    private TextView raceInfoText;
    private com.google.android.material.button.MaterialButton btnGoToPredict;
    private TextView cityNameText;
    private SeekBar opacitySeekBar;
    private TextView opacityValue;

    private DashboardViewModel viewModel;
    private List<Race> availableRaces = new ArrayList<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isGlobeRotating = true;

    // Mémoire pour la course sélectionnée
    private int currentSelectedRaceId = -1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_dashboard);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        setupObservers();
        viewModel.loadRaces(2026);

        globeWebView = findViewById(R.id.globeWebView);
        cityCard     = findViewById(R.id.cityCard);
        cityNameText = findViewById(R.id.cityNameText);
        raceInfoText = findViewById(R.id.raceInfoText);
        btnGoToPredict = findViewById(R.id.btnGoToPredict);
        View rootLayout   = findViewById(R.id.rootLayout);
        View statusSpacer = findViewById(R.id.statusBarSpacer);
        View navBarSpacer = findViewById(R.id.navBarSpacer);

        configureWebView();
        loadGlobe();
        registerBackHandler();
        setupOpacitySlider();
        setupRotationButton();
        setupNavBar();

        // On transmet l'ID dans l'Intent
        btnGoToPredict.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, PredictionActivity.class);
            if (currentSelectedRaceId != -1) {
                intent.putExtra("SELECTED_RACE_ID", currentSelectedRaceId);
            }
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams spTop = statusSpacer.getLayoutParams();
            spTop.height = sysBars.top;
            statusSpacer.setLayoutParams(spTop);
            ViewGroup.LayoutParams spBot = navBarSpacer.getLayoutParams();
            spBot.height = sysBars.bottom;
            navBarSpacer.setLayoutParams(spBot);
            return insets;
        });

        TextView tvLogo = findViewById(R.id.tvLogo);
        SpannableString logo = new SpannableString("F1 PREDICT");
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3030")), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#888888")), 2, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (tvLogo != null) tvLogo.setText(logo);
    }

    private void setupObservers() {
        viewModel.racesLiveData.observe(this, races -> {
            if (races != null) {
                this.availableRaces = races;
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

        for (int[] tab : tabs) {
            View tabView = findViewById(tab[0]);
            if (tabView == null) continue;

            tabView.setOnClickListener(v -> {
                for (int[] t : tabs) {
                    ImageView img = findViewById(t[1]);
                    TextView txt = findViewById(t[2]);
                    if (img != null) img.setColorFilter(inactiveColor);
                    if (txt != null) txt.setTextColor(inactiveColor);
                }
                Intent intent = null;
                if (tab[0] == R.id.tabPredict) intent = new Intent(this, PredictionActivity.class);
                else if (tab[0] == R.id.tabPodium) intent = new Intent(this, StandingsActivity.class);
                else if (tab[0] == R.id.tabDriver) intent = new Intent(this, ProfileActivity.class);

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }

    private void setupRotationButton() {
        FloatingActionButton btnRotate = findViewById(R.id.btnRotate);
        if (btnRotate == null) return;
        btnRotate.setOnClickListener(v -> {
            isGlobeRotating = !isGlobeRotating;
            globeWebView.evaluateJavascript("toggleRotation()", null);
            btnRotate.setImageResource(isGlobeRotating ? R.drawable.ic_pause : R.drawable.ic_play);
        });
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void configureWebView() {
        WebSettings settings = globeWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        globeWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        globeWebView.setBackgroundColor(0x00000000);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setDomStorageEnabled(true);
        globeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return true; }
        });
        globeWebView.addJavascriptInterface(new GlobeJSInterface(), "Android");
    }

    private void loadGlobe() {
        globeWebView.loadUrl("file:///android_asset/index.html");
    }

    private class GlobeJSInterface {
        @JavascriptInterface
        public void onCityClick(final String cityName) {
            mainHandler.post(() -> showCityInfo(cityName));
        }

        @JavascriptInterface
        public void onMapClick() {
            mainHandler.post(() -> hideCityInfo());
        }
    }

    private void showCityInfo(String mapLocationName) {
        String flag = RaceBottomSheet.getFlagEmoji(mapLocationName);
        cityNameText.setText(flag + " GP " + mapLocationName);

        String details = "Date à confirmer";
        currentSelectedRaceId = -1; // Réinitialise
        String query = mapLocationName.toLowerCase();

        for (Race r : availableRaces) {
            String rName = r.getName() != null ? r.getName().toLowerCase() : "";
            String rCountry = r.getCountry() != null ? r.getCountry().toLowerCase() : "";

            if (rName.contains(query) || rCountry.contains(query)) {
                details = "Circuit : " + r.getCircuit() + "\nDate : " + r.getDate();
                currentSelectedRaceId = r.getId(); // On mémorise la course
                break;
            }
        }

        raceInfoText.setText(details);

        if (cityCard.getVisibility() != View.VISIBLE || cityCard.getAlpha() < 1f) {
            cityCard.setVisibility(View.VISIBLE);
            cityCard.setAlpha(0f);
            cityCard.setTranslationY(40f);
            cityCard.animate().alpha(1f).translationY(0).setDuration(300).start();
        }
    }

    private void hideCityInfo() {
        if (cityCard.getVisibility() == View.VISIBLE) {
            cityCard.animate().alpha(0f).translationY(50f).setDuration(250).withEndAction(() -> cityCard.setVisibility(View.GONE)).start();
        }
    }

    @Override protected void onResume() { super.onResume(); globeWebView.onResume(); globeWebView.resumeTimers(); }
    @Override protected void onPause() { super.onPause(); globeWebView.onPause(); globeWebView.pauseTimers(); }
    @Override protected void onDestroy() { super.onDestroy(); globeWebView.destroy(); }

    private void setupOpacitySlider() {
        opacitySeekBar = findViewById(R.id.opacitySeekBar);
        opacityValue   = findViewById(R.id.opacityValue);
        if(opacitySeekBar == null) return;
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float opacity = progress / 100f;
                globeWebView.evaluateJavascript("setEarthOpacity(" + opacity + ")", null);
                if (opacityValue != null) opacityValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void registerBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (globeWebView.canGoBack()) globeWebView.goBack();
                else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}