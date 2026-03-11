package com.example.prediction_score_gp.data.repository;

import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.User;
import retrofit2.Call;
import java.util.Map;

public class AuthRepository {

    public Call<User> login(String email, String password) {
        Map<String, String> body = Map.of("email", email, "password", password);
        return RetrofitClient.getAuthApi().login(body);
    }

    public Call<User> register(String email, String password, String username) {
        Map<String, String> body = Map.of("email", email, "password", password, "username", username);
        return RetrofitClient.getAuthApi().register(body);
    }

    public Call<User> getMe() {
        return RetrofitClient.getAuthApi().getMe();
    }
}
