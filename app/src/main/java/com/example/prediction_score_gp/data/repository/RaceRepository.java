package com.example.prediction_score_gp.data.repository;

import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.Race;
import retrofit2.Call;
import java.util.List;

public class RaceRepository {

    public Call<List<Race>> getRaces(Integer season) {
        return RetrofitClient.getRacesApi().getRaces(season);
    }

    public Call<Race> getRaceById(int id) {
        return RetrofitClient.getRacesApi().getRaceById(id);
    }
}
