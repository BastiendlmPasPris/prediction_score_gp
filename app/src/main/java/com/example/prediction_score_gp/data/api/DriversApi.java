package com.example.prediction_score_gp.data.api;

import com.example.prediction_score_gp.data.model.Driver;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface DriversApi {

    @GET("drivers")
    Call<List<Driver>> getDrivers();

    @GET("drivers/{id}")
    Call<Driver> getDriverById(@Path("id") int id);

    @GET("drivers/{id}/stats")
    Call<Map<String, Object>> getDriverStats(@Path("id") int id);
}
