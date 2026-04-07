package com.example.prediction_score_gp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.prediction_score_gp.BuildConfig;
import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.User;

/**
 * Source de vérité unique pour la session utilisateur (JWT + profil + préférences).
 * Toutes les lectures/écritures de session passent par cette classe.
 */
public class SessionManager {

    private static final String PREFS_NAME   = "f1predict_session";
    private static final String KEY_TOKEN    = "token";
    private static final String KEY_ID       = "user_id";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE     = "role";
    private static final String KEY_API_URL  = "api_url";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── Session utilisateur ───────────────────────────────────────────────────

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
        // Conserver l'URL API lors de la déconnexion
        String savedUrl = getApiUrl(context);
        getPrefs(context).edit().clear().apply();
        if (!savedUrl.equals(BuildConfig.API_BASE_URL)) {
            setApiUrl(context, savedUrl);
        }
        RetrofitClient.setToken(null);
    }

    /**
     * Restaure le token ET l'URL API depuis les SharedPreferences dans RetrofitClient.
     * À appeler au démarrage de l'application (LoginActivity.onCreate).
     */
    public static void restoreToken(Context context) {
        // 1. Restaurer l'URL API personnalisée si elle existe
        String apiUrl = getPrefs(context).getString(KEY_API_URL, null);
        if (apiUrl != null && !apiUrl.isEmpty()) {
            RetrofitClient.setBaseUrl(apiUrl);
        }
        // 2. Restaurer le token JWT
        String token = getPrefs(context).getString(KEY_TOKEN, null);
        if (token != null) RetrofitClient.setToken(token);
    }

    // ── URL API configurable ─────────────────────────────────────────────────

    /**
     * Retourne l'URL API configurée par l'utilisateur,
     * ou BuildConfig.API_BASE_URL par défaut.
     */
    public static String getApiUrl(Context context) {
        return getPrefs(context).getString(KEY_API_URL, BuildConfig.API_BASE_URL);
    }

    /**
     * Sauvegarde une URL API personnalisée et l'applique immédiatement à RetrofitClient.
     * Permet d'utiliser l'app sur un vrai téléphone en pointant vers l'IP réelle du serveur.
     */
    public static void setApiUrl(Context context, String url) {
        if (url == null || url.isEmpty()) return;
        if (!url.endsWith("/")) url = url + "/";
        getPrefs(context).edit().putString(KEY_API_URL, url).apply();
        RetrofitClient.setBaseUrl(url);
    }
}
