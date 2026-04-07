package com.example.prediction_score_gp.ui;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prediction_score_gp.util.LocaleHelper;

/**
 * Classe de base pour toutes les Activities.
 * Surcharge attachBaseContext pour appliquer la locale choisie par l'utilisateur.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
