package com.example.prediction_score_gp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.local.SessionManager;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;
import com.example.prediction_score_gp.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restaurer le token persisté dans RetrofitClient
        SessionManager.restoreToken(this);

        // Auto-login si session existante
        if (SessionManager.hasSession(this)) {
            goToDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.userLiveData.observe(this, user -> {
            if (user != null) {
                SessionManager.save(this, user);
                goToDashboard();
            }
        });

        viewModel.errorLiveData.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.loadingLiveData.observe(this, loading -> {
            if (loading == null) return;
            btnLogin.setEnabled(!loading);
            if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        TextView tvRegister = findViewById(R.id.tvRegister);
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));
        }
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
