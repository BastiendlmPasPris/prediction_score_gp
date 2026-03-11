package com.example.prediction_score_gp.data.api;

import com.example.prediction_score_gp.data.model.Prediction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface PredictApi {

    @POST("predict")
    Call<Prediction> predict(@Body Map<String, Integer> body);

    @POST("predict/race/{race_id}")
    Call<List<Prediction>> predictRace(@Path("race_id") int raceId);

    @GET("predictions/history")
    Call<List<Prediction>> getPredictionHistory();
}
