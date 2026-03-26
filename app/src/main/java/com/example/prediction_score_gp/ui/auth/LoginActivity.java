package com.example.prediction_score_gp.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_login);

        setupLogo();
        bindViews();
        setupViewModel();
        setupClickListeners();
    }

    // ── Logo bicolore "F1" rouge + " PREDICT" gris ───────────────────
    private void setupLogo() {
        TextView tvLogo = findViewById(R.id.tvLogo);
        SpannableString logo = new SpannableString("F1 PREDICT");
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3030")),
                0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#888888")),
                2, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLogo.setText(logo);
    }

    // ── Lier les vues ────────────────────────────────────────────────
    private void bindViews() {
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvError     = findViewById(R.id.tvError);
        progressBar = findViewById(R.id.progressBar);
    }

    // ── ViewModel + observers ────────────────────────────────────────
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Succès → aller au Dashboard
        viewModel.userLiveData.observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        });

        // Erreur → afficher le message
        viewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                tvError.setText(error);
                tvError.setVisibility(View.VISIBLE);
                // Souligner le champ en erreur
                tilEmail.setBoxStrokeColor(Color.parseColor("#FF3030"));
                tilPassword.setBoxStrokeColor(Color.parseColor("#FF3030"));
            }
        });

        // Loading → spinner + désactiver le bouton
        viewModel.loadingLiveData.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
            btnLogin.setAlpha(isLoading ? 0.5f : 1f);
        });
    }

    // ── Listeners ────────────────────────────────────────────────────
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Lien vers RegisterActivity
        findViewById(R.id.tvGoRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(0, 0);
        });

        // Reset erreur au focus
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearErrors();
        });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearErrors();
        });
    }

    // ── Validation + appel ViewModel ────────────────────────────────
    private void attemptLogin() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (email.isEmpty()) {
            tilEmail.setError("Email requis");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Mot de passe requis");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("6 caractères minimum");
            return;
        }

        clearErrors();
        viewModel.login(email, password);
    }

    private void clearErrors() {
        tvError.setVisibility(View.GONE);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilEmail.setBoxStrokeColor(Color.parseColor("#2A2A2A"));
        tilPassword.setBoxStrokeColor(Color.parseColor("#2A2A2A"));
    }
}