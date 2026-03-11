package com.example.prediction_score_gp.data.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/"; // localhost depuis émulateur
    private static Retrofit retrofit = null;
    private static String jwtToken = null;

    public static void setToken(String token) {
        jwtToken = token;
        retrofit = null; // reset pour recréer avec le token
    }

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

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApi getAuthApi() {
        return getInstance().create(AuthApi.class);
    }

    public static RacesApi getRacesApi() {
        return getInstance().create(RacesApi.class);
    }

    public static DriversApi getDriversApi() {
        return getInstance().create(DriversApi.class);
    }

    public static PredictApi getPredictApi() {
        return getInstance().create(PredictApi.class);
    }

    public static AdminApi getAdminApi() {
        return getInstance().create(AdminApi.class);
    }
}
