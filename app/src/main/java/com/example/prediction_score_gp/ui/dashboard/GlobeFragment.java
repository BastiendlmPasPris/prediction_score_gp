package com.example.prediction_score_gp.ui.dashboard;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prediction_score_gp.MainActivity;
import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.ui.prediction.RaceBottomSheet;
import com.example.prediction_score_gp.viewmodel.DashboardViewModel;
import com.example.prediction_score_gp.viewmodel.SharedViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GlobeFragment extends Fragment {

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
    private int currentSelectedRaceId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_globe, container, false);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        globeWebView  = view.findViewById(R.id.globeWebView);
        cityCard      = view.findViewById(R.id.cityCard);
        cityNameText  = view.findViewById(R.id.cityNameText);
        raceInfoText  = view.findViewById(R.id.raceInfoText);
        btnGoToPredict = view.findViewById(R.id.btnGoToPredict);
        View rootLayout   = view.findViewById(R.id.rootLayout);
        View statusSpacer = view.findViewById(R.id.statusBarSpacer);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.racesLiveData.observe(getViewLifecycleOwner(), races -> {
            if (races != null) availableRaces = races;
        });
        viewModel.loadRaces(2026);

        configureWebView();
        loadGlobe();
        setupOpacitySlider(view);
        setupRotationButton(view);
        setupLogoText(view);
        registerBackHandler();

        btnGoToPredict.setOnClickListener(v -> {
            SharedViewModel sharedVm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            if (currentSelectedRaceId != -1) {
                sharedVm.setSelectedRaceId(currentSelectedRaceId);
            }
            ((MainActivity) requireActivity()).switchToTab(1);
        });

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (statusSpacer != null) {
                ViewGroup.LayoutParams p = statusSpacer.getLayoutParams();
                p.height = sysBars.top;
                statusSpacer.setLayoutParams(p);
            }
            return insets;
        });
    }

    private void setupLogoText(View view) {
        TextView tvLogo = view.findViewById(R.id.tvLogo);
        SpannableString logo = new SpannableString("F1 PREDICT");
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3030")), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#888888")), 2, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (tvLogo != null) tvLogo.setText(logo);
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
        currentSelectedRaceId = -1;
        String query = mapLocationName.toLowerCase();

        for (Race r : availableRaces) {
            String rName    = r.getName()    != null ? r.getName().toLowerCase()    : "";
            String rCountry = r.getCountry() != null ? r.getCountry().toLowerCase() : "";
            if (rName.contains(query) || rCountry.contains(query)) {
                details = "Circuit : " + r.getCircuit() + "\nDate : " + r.getDate();
                currentSelectedRaceId = r.getId();
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
            cityCard.animate().alpha(0f).translationY(50f).setDuration(250)
                    .withEndAction(() -> cityCard.setVisibility(View.GONE)).start();
        }
    }

    private void setupRotationButton(View view) {
        FloatingActionButton btnRotate = view.findViewById(R.id.btnRotate);
        if (btnRotate == null) return;
        btnRotate.setOnClickListener(v -> {
            isGlobeRotating = !isGlobeRotating;
            globeWebView.evaluateJavascript("toggleRotation()", null);
            btnRotate.setImageResource(isGlobeRotating ? R.drawable.ic_pause : R.drawable.ic_play);
        });
    }

    private void setupOpacitySlider(View view) {
        opacitySeekBar = view.findViewById(R.id.opacitySeekBar);
        opacityValue   = view.findViewById(R.id.opacityValue);
        if (opacitySeekBar == null) return;
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
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (globeWebView != null && globeWebView.canGoBack()) {
                            globeWebView.goBack();
                        } else {
                            setEnabled(false);
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (globeWebView != null) { globeWebView.onResume(); globeWebView.resumeTimers(); }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (globeWebView != null) { globeWebView.onPause(); globeWebView.pauseTimers(); }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (globeWebView != null) globeWebView.destroy();
    }
}
