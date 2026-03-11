package com.example.prediction_score_gp.data.api;

import com.example.prediction_score_gp.data.model.Race;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;

public interface RacesApi {

    @GET("races")
    Call<List<Race>> getRaces(@Query("season") Integer season);

    @GET("races/{id}")
    Call<Race> getRaceById(@Path("id") int id);
}
