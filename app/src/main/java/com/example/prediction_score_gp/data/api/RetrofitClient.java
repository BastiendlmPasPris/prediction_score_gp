package com.example.prediction_score_gp.data.api;

import com.example.prediction_score_gp.BuildConfig;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client Retrofit configurable.
 * L'URL de base est initialisée depuis BuildConfig.API_BASE_URL
 * (debug = 10.0.2.2, release = URL du serveur réel).
 * Elle peut être surchargée à l'exécution via setBaseUrl() pour
 * permettre la configuration sur un vrai téléphone (voir SessionManager).
 */
public class RetrofitClient {

    /** URL par défaut injectée à la compilation (voir build.gradle.kts). */
    private static String currentBaseUrl = BuildConfig.API_BASE_URL;

    private static Retrofit retrofit = null;
    private static String jwtToken   = null;

    // ── Token JWT ────────────────────────────────────────────────────────────

    public static void setToken(String token) {
        jwtToken = token;
        retrofit = null; // force la recréation avec le nouveau token
    }

    public static String getToken() {
        return jwtToken;
    }

    // ── URL de base ──────────────────────────────────────────────────────────

    /**
     * Modifie l'URL de base utilisée par Retrofit.
     * L'URL doit se terminer par '/'.
     * Appeler cette méthode réinitialise l'instance Retrofit.
     */
    public static void setBaseUrl(String url) {
        if (url == null || url.isEmpty()) return;
        if (!url.endsWith("/")) url = url + "/";
        currentBaseUrl = url;
        retrofit = null; // force la recréation
    }

    public static String getBaseUrl() {
        return currentBaseUrl;
    }

    // ── Singleton Retrofit ────────────────────────────────────────────────────

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json");
                        if (jwtToken != null) {
                            requestBuilder.header("Authorization", "Bearer " + jwtToken);
                        }
                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    // ── Factories d'API ───────────────────────────────────────────────────────

    public static AuthApi getAuthApi()         { return getInstance().create(AuthApi.class); }
    public static RacesApi getRacesApi()       { return getInstance().create(RacesApi.class); }
    public static DriversApi getDriversApi()   { return getInstance().create(DriversApi.class); }
    public static PredictApi getPredictApi()   { return getInstance().create(PredictApi.class); }
    public static AdminApi getAdminApi()       { return getInstance().create(AdminApi.class); }
}
