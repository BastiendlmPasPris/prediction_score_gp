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

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText etUsername, etEmail, etPassword;
    private TextInputLayout tilUsername, tilEmail, tilPassword;
    private MaterialButton btnRegister;
    private TextView tvError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_register);

        setupLogo();
        bindViews();
        setupViewModel();
        setupClickListeners();
    }

    private void setupLogo() {
        TextView tvLogo = findViewById(R.id.tvLogo);
        SpannableString logo = new SpannableString("F1 PREDICT");
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3030")),
                0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        logo.setSpan(new ForegroundColorSpan(Color.parseColor("#888888")),
                2, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLogo.setText(logo);
    }

    private void bindViews() {
        etUsername  = findViewById(R.id.etUsername);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvError     = findViewById(R.id.tvError);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Succès → Dashboard
        viewModel.userLiveData.observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, DashboardActivity.class));
                finishAffinity(); // Ferme Login + Register
            }
        });

        // Erreur
        viewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                tvError.setText(error);
                tvError.setVisibility(View.VISIBLE);
            }
        });

        // Loading
        viewModel.loadingLiveData.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
            btnRegister.setAlpha(isLoading ? 0.5f : 1f);
        });
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        // Retour vers Login
        findViewById(R.id.tvGoLogin).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Reset erreurs au focus
        etUsername.setOnFocusChangeListener((v, f) -> { if (f) clearErrors(); });
        etEmail   .setOnFocusChangeListener((v, f) -> { if (f) clearErrors(); });
        etPassword.setOnFocusChangeListener((v, f) -> { if (f) clearErrors(); });
    }

    private void attemptRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString()        : "";

        if (username.isEmpty()) {
            tilUsername.setError("Nom d'utilisateur requis");
            return;
        }
        if (username.length() < 3) {
            tilUsername.setError("3 caractères minimum");
            return;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email requis");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email invalide");
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
        viewModel.register(email, password, username);
    }

    private void clearErrors() {
        tvError.setVisibility(View.GONE);
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
    }
}