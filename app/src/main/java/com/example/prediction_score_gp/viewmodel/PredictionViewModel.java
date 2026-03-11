package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.data.repository.PredictRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class PredictionViewModel extends ViewModel {

    private final PredictRepository repository = new PredictRepository();

    public MutableLiveData<Prediction> predictionLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Prediction>> standingsLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

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
