package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.data.repository.RaceRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final RaceRepository raceRepository = new RaceRepository();

    public MutableLiveData<Race> nextRaceLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Race>> racesLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void loadRaces(int season) {
        raceRepository.getRaces(season).enqueue(new Callback<List<Race>>() {
            @Override
            public void onResponse(Call<List<Race>> call, Response<List<Race>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    racesLiveData.setValue(response.body());
                    // TODO: Identifier le prochain GP et le mettre dans nextRaceLiveData
                }
            }
            @Override
            public void onFailure(Call<List<Race>> call, Throwable t) {
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }
}
