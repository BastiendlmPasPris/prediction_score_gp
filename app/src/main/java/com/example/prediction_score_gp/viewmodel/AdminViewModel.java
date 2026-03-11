package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prediction_score_gp.data.api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.Map;

public class AdminViewModel extends ViewModel {

    public MutableLiveData<Map<String, Object>> metricsLiveData = new MutableLiveData<>();
    public MutableLiveData<List<String>> versionsLiveData = new MutableLiveData<>();
    public MutableLiveData<String> trainStatusLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void trainModel() {
        Map<String, Object> body = Map.of("target", "podium", "test_size", 0.2);
        RetrofitClient.getAdminApi().trainModel(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) trainStatusLiveData.setValue("Entraînement lancé");
                else errorLiveData.setValue("Erreur lancement entraînement");
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public void loadMetrics() {
        RetrofitClient.getAdminApi().getModelMetrics().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) metricsLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public void loadVersions() {
        RetrofitClient.getAdminApi().getModelVersions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) versionsLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }
}
