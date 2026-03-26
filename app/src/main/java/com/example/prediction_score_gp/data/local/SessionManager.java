package com.example.prediction_score_gp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.User;

/**
 * Source de vérité unique pour la session utilisateur (JWT + profil).
 * Toutes les lectures/écritures de session passent par cette classe.
 */
public class SessionManager {

    private static final String PREFS_NAME = "f1predict_session";
    private static final String KEY_TOKEN    = "token";
    private static final String KEY_ID       = "user_id";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE     = "role";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Persiste la session et injecte le token dans RetrofitClient. */
    public static void save(Context context, User user) {
        getPrefs(context).edit()
                .putString(KEY_TOKEN,    user.getToken())
                .putInt   (KEY_ID,       user.getId())
                .putString(KEY_EMAIL,    user.getEmail())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_ROLE,     user.getRole())
                .apply();
        RetrofitClient.setToken(user.getToken());
    }

    /** Reconstruit un User depuis les SharedPreferences. */
    public static User getUser(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String token = prefs.getString(KEY_TOKEN, null);
        if (token == null) return null;
        return new User(
                prefs.getInt   (KEY_ID,       0),
                prefs.getString(KEY_EMAIL,    ""),
                prefs.getString(KEY_USERNAME, ""),
                prefs.getString(KEY_ROLE,     "user"),
                token
        );
    }

    /** Retourne vrai si un token valide est stocké. */
    public static boolean hasSession(Context context) {
        String token = getPrefs(context).getString(KEY_TOKEN, null);
        return token != null && !token.isEmpty();
    }

    /** Efface la session et réinitialise RetrofitClient. */
    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
        RetrofitClient.setToken(null);
    }

    /** Restaure le token depuis les SharedPreferences dans RetrofitClient (à appeler au démarrage). */
    public static void restoreToken(Context context) {
        String token = getPrefs(context).getString(KEY_TOKEN, null);
        if (token != null) RetrofitClient.setToken(token);
    }
}
