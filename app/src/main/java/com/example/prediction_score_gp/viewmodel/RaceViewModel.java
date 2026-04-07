package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.data.repository.RaceRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RaceViewModel extends ViewModel {

    // C'est ici que l'Activity va "écouter" les données
    public MutableLiveData<List<Race>> racesLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private RaceRepository raceRepository;

    public RaceViewModel() {
        raceRepository = new RaceRepository();
    }

    public void loadRaces(int season) {
        raceRepository.getRaces(season).enqueue(new Callback<List<Race>>() {
            @Override
            public void onResponse(Call<List<Race>> call, Response<List<Race>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // On envoie la liste des courses au LiveData
                    racesLiveData.postValue(response.body());
                } else {
                    errorLiveData.postValue("Erreur lors de la récupération des courses");
                }
            }

            @Override
            public void onFailure(Call<List<Race>> call, Throwable t) {
                errorLiveData.postValue("Connexion impossible : " + t.getMessage());
            }
        });
    }
}