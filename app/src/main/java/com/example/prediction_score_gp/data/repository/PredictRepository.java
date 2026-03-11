package com.example.prediction_score_gp.data.repository;

import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.Prediction;
import retrofit2.Call;
import java.util.List;
import java.util.Map;

public class PredictRepository {

    public Call<Prediction> predict(int raceId, int driverId) {
        Map<String, Integer> body = Map.of("race_id", raceId, "driver_id", driverId);
        return RetrofitClient.getPredictApi().predict(body);
    }

    public Call<List<Prediction>> predictRace(int raceId) {
        return RetrofitClient.getPredictApi().predictRace(raceId);
    }

    public Call<List<Prediction>> getHistory() {
        return RetrofitClient.getPredictApi().getPredictionHistory();
    }
}
