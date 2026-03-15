package com.example.prediction_score_gp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.ui.dashboard.DashboardActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Récupérer le bouton par son ID
        Button btnLogin = findViewById(R.id.btnLogin);

        // 2. Écouter l'événement "clic"
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Créer l'Intent pour aller vers DashboardActivity
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);

                // Optionnel : fermer LoginActivity pour éviter de revenir en arrière avec le bouton 'Back'
                // finish();
            }
        });

        // TODO: Initialiser les vues (email, password, bouton login)
        // TODO: Connecter au LoginViewModel
        // TODO: Gérer la navigation vers DashboardActivity après succès
        // TODO: Gérer la navigation vers RegisterActivity
    }
}
