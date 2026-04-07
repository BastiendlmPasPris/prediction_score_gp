package com.example.prediction_score_gp.ui.dashboard;

// ─────────────────────────────────────────────────────────────────────────────
//  DashBoardActivity.java
//  Configure la WebView pour afficher le globe Three.js
//  et reçoit les clics de villes via une JavascriptInterface.
//
//  Fonctionnalités Android spécifiques :
//    • Capteur de luminosité ambiante → ajustement automatique de la luminosité
//    • Navigation par swipe gauche/droite entre les onglets
//    • Retour haptique sur les boutons
// ─────────────────────────────────────────────────────────────────────────────

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
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

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.ui.BaseActivity;
import com.example.prediction_score_gp.ui.prediction.PredictionActivity;
import com.example.prediction_score_gp.ui.profile.ProfileActivity;
import com.example.prediction_score_gp.ui.standings.StandingsActivity;
import com.example.prediction_score_gp.util.HapticHelper;
import com.example.prediction_score_gp.util.LightSensorHelper;
import com.example.prediction_score_gp.util.SwipeNavigationHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;


public class DashboardActivity extends BaseActivity {

    private WebView globeWebView;
    private MaterialCardView cityCard;
    private TextView raceInfoText;
    private com.google.android.material.button.MaterialButton btnGoToPredict;
    private android.widget.TextView cityNameText;
    private SeekBar opacitySeekBar;
    private android.widget.TextView opacityValue;

    // Capteur de luminosité ambiante
    private LightSensorHelper lightSensorHelper;

    // Handler principal pour poster sur l'UI thread depuis le thread JS
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isGlobeRotating = true;

    // Ordre des onglets : 0=Globe 1=Predict 2=Podium 3=Driver
    private static final int TAB_INDEX = 0;

    // ─────────────────────────────────────────────────────────
    //  Création de l'Activity
    // ─────────────────────────────────────────────────────────
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Plein écran immersif (barre de statut transparente)
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_dashboard);

        // ── Vues ─────────────────────────────────────────────────
        globeWebView = findViewById(R.id.globeWebView);
        cityCard     = findViewById(R.id.cityCard);
        cityNameText = findViewById(R.id.cityNameText);
        View rootLayout   = findViewById(R.id.rootLayout);
        View statusSpacer = findViewById(R.id.statusBarSpacer);
        View navBarSpacer = findViewById(R.id.navBarSpacer);

        // ── WebView ───────────────────────────────────────────────
        configureWebView();
        loadGlobe();
        registerBackHandler();
        setupOpacitySlider();

        raceInfoText = findViewById(R.id.raceInfoText);
        btnGoToPredict = findViewById(R.id.btnGoToPredict);
        btnGoToPredict.setOnClickListener(v -> {
            HapticHelper.tap(v);
            Intent intent = new Intent(DashboardActivity.this, PredictionActivity.class);
            intent.putExtra("SELECTED_CITY", cityNameText.getText().toString());
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // ── WindowInsets : status bar + nav bar ──────────────────
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

        // ── Logo "F1" rouge + " PREDICT" gris ────────────────────
        TextView tvLogo = findViewById(R.id.tvLogo);
        SpannableString logo = new SpannableString("F1 PREDICT");
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3030")),
                0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#888888")),
                2, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLogo.setText(logo);

        // ── Nav bar : onglets ─────────────────────────────────────
        setupNavBar();

        // ── Bouton pause/play rotation ────────────────────────────
        setupRotationButton();

        // ── Capteur luminosité ────────────────────────────────────
        lightSensorHelper = new LightSensorHelper(this, getWindow());

        // ── Navigation par swipe ──────────────────────────────────
        SwipeNavigationHelper.attach(rootLayout, new SwipeNavigationHelper.OnSwipeCallback() {
            @Override
            public void onSwipeLeft() {  // → Predict (onglet suivant)
                HapticHelper.tap(rootLayout);
                startActivity(new Intent(DashboardActivity.this, PredictionActivity.class));
                overridePendingTransition(0, 0);
            }

            @Override
            public void onSwipeRight() { // pas d'onglet précédent sur Globe
                HapticHelper.tap(rootLayout);
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
                HapticHelper.tap(v);

                for (int[] t : tabs) {
                    ImageView img = findViewById(t[1]);
                    TextView txt = findViewById(t[2]);
                    if (img != null) img.setColorFilter(inactiveColor);
                    if (txt != null) txt.setTextColor(inactiveColor);
                }

                Intent intent = null;
                if (tab[0] == R.id.tabPredict) {
                    intent = new Intent(this, PredictionActivity.class);
                } else if (tab[0] == R.id.tabPodium) {
                    intent = new Intent(this, StandingsActivity.class);
                } else if (tab[0] == R.id.tabDriver) {
                    intent = new Intent(this, ProfileActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void setupRotationButton() {
        FloatingActionButton btnRotate = findViewById(R.id.btnRotate);

        btnRotate.setOnClickListener(v -> {
            HapticHelper.tap(v);
            isGlobeRotating = !isGlobeRotating;
            globeWebView.evaluateJavascript("toggleRotation()", null);
            btnRotate.setImageResource(isGlobeRotating ? R.drawable.ic_pause : R.drawable.ic_play);
        });
    }

    // ─────────────────────────────────────────────────────────
    //  Configuration de la WebView
    // ─────────────────────────────────────────────────────────
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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true; // bloque toute navigation externe
            }
        });

        globeWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
                android.util.Log.d("GlobeJS",
                        "[" + cm.sourceId() + ":" + cm.lineNumber() + "] " + cm.message());
                return true;
            }
        });

        globeWebView.addJavascriptInterface(new GlobeJSInterface(), "Android");
    }

    // ─────────────────────────────────────────────────────────
    //  Chargement de l'HTML depuis les assets
    // ─────────────────────────────────────────────────────────
    private void loadGlobe() {
        globeWebView.loadUrl("file:///android_asset/index.html");
    }

    // ─────────────────────────────────────────────────────────
    //  JavascriptInterface
    // ─────────────────────────────────────────────────────────
    private class GlobeJSInterface {
        @JavascriptInterface
        public void onCityClick(final String cityName) {
            mainHandler.post(() -> showCityInfo(cityName));
        }

        @JavascriptInterface
        public void onMapClick() {
            mainHandler.post(() -> hideCityInfo());
        }

        @JavascriptInterface
        public void logError(String message) {
            android.util.Log.e("GlobeApp", "JS Error: " + message);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Affichage permanent de la carte
    // ─────────────────────────────────────────────────────────
    private void showCityInfo(String cityName) {
        cityNameText.setText(cityName + " GP");
        raceInfoText.setText(getString(R.string.text_race_details_placeholder));

        if (cityCard.getVisibility() != View.VISIBLE || cityCard.getAlpha() < 1f) {
            cityCard.setVisibility(View.VISIBLE);
            cityCard.setAlpha(0f);
            cityCard.setTranslationY(40f);
            cityCard.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }
        HapticHelper.tap(cityCard);
    }

    // ─────────────────────────────────────────────────────────
    //  Masquage de la carte
    // ─────────────────────────────────────────────────────────
    private void hideCityInfo() {
        if (cityCard.getVisibility() == View.VISIBLE) {
            cityCard.animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(250)
                    .withEndAction(() -> cityCard.setVisibility(View.GONE))
                    .start();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Cycle de vie : gestion de la WebView + capteur
    // ─────────────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        globeWebView.onResume();
        globeWebView.resumeTimers();
        if (lightSensorHelper != null) lightSensorHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        globeWebView.onPause();
        globeWebView.pauseTimers();
        if (lightSensorHelper != null) lightSensorHelper.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globeWebView.destroy();
    }

    // ─────────────────────────────────────────────────────────
    //  Slider opacité texture Terre
    // ─────────────────────────────────────────────────────────
    private void setupOpacitySlider() {
        opacitySeekBar = findViewById(R.id.opacitySeekBar);
        opacityValue   = findViewById(R.id.opacityValue);

        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float opacity = progress / 100f;
                globeWebView.evaluateJavascript("setEarthOpacity(" + opacity + ")", null);
                opacityValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) { HapticHelper.tap(s); }
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    // Bouton Retour
    private void registerBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (globeWebView.canGoBack()) {
                    globeWebView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    //  Propagation des touch events (swipe + WebView)
    // ─────────────────────────────────────────────────────────
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }
}
