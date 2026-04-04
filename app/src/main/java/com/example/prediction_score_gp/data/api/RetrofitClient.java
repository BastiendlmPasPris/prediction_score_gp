package com.example.prediction_score_gp.data.api;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;
    private static String jwtToken = null;

    /**
     * Définit le token et force la recréation de l'instance Retrofit
     */
    public static void setToken(String token) {
        Log.d("RetrofitClient", "Setting new JWT Token: " + (token != null ? "Present" : "Null"));
        jwtToken = token;
        retrofit = null; // Important : on invalide l'instance pour forcer l'intercepteur à se mettre à jour
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Logger pour le débug
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json");

                        if (jwtToken != null && !jwtToken.isEmpty()) {
                            // On injecte le header d'authentification
                            requestBuilder.header("Authorization", "Bearer " + jwtToken);
                            Log.d("RetrofitClient", "Request with Token: " + original.url());
                        } else {
                            Log.w("RetrofitClient", "Request WITHOUT Token: " + original.url());
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // --- Helpers pour les APIs ---

    public static AuthApi getAuthApi() { return getInstance().create(AuthApi.class); }
    public static RacesApi getRacesApi() { return getInstance().create(RacesApi.class); }
    public static DriversApi getDriversApi() { return getInstance().create(DriversApi.class); }
    public static PredictApi getPredictApi() { return getInstance().create(PredictApi.class); }
    public static AdminApi getAdminApi() { return getInstance().create(AdminApi.class); }
}