package com.example.prediction_score_gp.data.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface AdminApi {

    @POST("admin/train")
    Call<Map<String, Object>> trainModel(@Body Map<String, Object> body);

    @GET("admin/eval")
    Call<Map<String, Object>> getModelMetrics();

    @GET("admin/model/versions")
    Call<List<String>> getModelVersions();

    @POST("admin/model/rollback/{version}")
    Call<Map<String, Object>> rollback(@Path("version") String version);

    @GET("admin/stats")
    Call<Map<String, Object>> getApiStats();
}
