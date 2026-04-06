package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prediction_score_gp.data.api.DriversApi;
import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.data.repository.PredictRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class PredictionViewModel extends ViewModel {

    private final PredictRepository repository = new PredictRepository();
    private final DriversApi driversApi = RetrofitClient.getInstance().create(DriversApi.class);

    public MutableLiveData<Prediction> predictionLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Prediction>> standingsLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Driver>> driversLiveData = new MutableLiveData<>(); // <--- AJOUTÉ
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void loadDrivers() {

        // Si la liste contient déjà des pilotes, on arrête la fonction immédiatement.
        // Aucune nouvelle requête réseau ne sera faite !
        if (driversLiveData.getValue() != null && !driversLiveData.getValue().isEmpty()) {
            return;
        }

        driversApi.getDrivers().enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    driversLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Impossible de charger les pilotes");
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                errorLiveData.setValue("Erreur réseau (Pilotes) : " + t.getMessage());
            }
        });
    }

    public void predict(int raceId, int driverId) {
        loadingLiveData.setValue(true);
        repository.predict(raceId, driverId).enqueue(new Callback<Prediction>() {
            @Override
            public void onResponse(Call<Prediction> call, Response<Prediction> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful()) predictionLiveData.setValue(response.body());
                else errorLiveData.setValue("Erreur lors de la prédiction");
            }
            @Override
            public void onFailure(Call<Prediction> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public void predictFullRace(int raceId) {
        loadingLiveData.setValue(true);
        repository.predictRace(raceId).enqueue(new Callback<List<Prediction>>() {
            @Override
            public void onResponse(Call<List<Prediction>> call, Response<List<Prediction>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful()) standingsLiveData.setValue(response.body());
                else errorLiveData.setValue("Erreur classement");
            }
            @Override
            public void onFailure(Call<List<Prediction>> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }
}