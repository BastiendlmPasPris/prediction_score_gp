package com.example.prediction_score_gp.data.api;

import com.example.prediction_score_gp.data.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import java.util.Map;

public interface AuthApi {

    @POST("auth/register")
    Call<User> register(@Body Map<String, String> body);

    @POST("auth/login")
    Call<User> login(@Body Map<String, String> body);

    @GET("auth/me")
    Call<User> getMe();

    @PUT("auth/me")
    Call<User> updateMe(@Body Map<String, String> body);
}
