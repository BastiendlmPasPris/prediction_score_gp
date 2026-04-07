package com.example.prediction_score_gp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Gère le changement de langue en temps réel sans modifier le paramètre système.
 * Usage :
 *   - dans BaseActivity.attachBaseContext() → LocaleHelper.wrap(base)
 *   - pour changer : LocaleHelper.setLanguage(context, "fr") puis recreate()
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "f1predict_settings";
    private static final String KEY_LANGUAGE = "language";
    public  static final String LANG_EN = "en";
    public  static final String LANG_FR = "fr";

    /** Retourne la langue sauvegardée (défaut : "en"). */
    public static String getLanguage(Context context) {
        return getPrefs(context).getString(KEY_LANGUAGE, LANG_EN);
    }

    /** Sauvegarde la langue choisie. Appelé avant recreate(). */
    public static void setLanguage(Context context, String lang) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, lang).apply();
    }

    /**
     * Retourne un Context dont la configuration de locale a été surchargée.
     * À appeler dans attachBaseContext() de chaque Activity/Application.
     */
    public static Context wrap(Context context) {
        String lang   = getLanguage(context);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
