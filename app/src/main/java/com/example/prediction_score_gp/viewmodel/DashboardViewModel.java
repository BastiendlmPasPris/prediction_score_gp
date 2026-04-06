package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.data.repository.RaceRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardViewModel extends ViewModel {

    private final RaceRepository raceRepository = new RaceRepository();

    public MutableLiveData<Race> nextRaceLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Race>> racesLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void loadRaces(int season) {
        // Système de Cache : Pas besoin de re-télécharger si on a déjà les données
        if (racesLiveData.getValue() != null && !racesLiveData.getValue().isEmpty()) {
            return;
        }

        raceRepository.getRaces(season).enqueue(new Callback<List<Race>>() {
            @Override
            public void onResponse(Call<List<Race>> call, Response<List<Race>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Race> races = response.body();
                    racesLiveData.setValue(races);

                    // Calculer le Prochain Grand Prix
                    findNextRace(races);
                }
            }
            @Override
            public void onFailure(Call<List<Race>> call, Throwable t) {
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void findNextRace(List<Race> races) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        Race nextRace = null;

        for (Race r : races) {
            try {
                Date raceDate = sdf.parse(r.getDate());
                // On prend la première course qui n'est pas encore passée
                if (raceDate != null && raceDate.after(today)) {
                    nextRace = r;
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Si la saison est finie, on affiche la dernière course
        if (nextRace == null && !races.isEmpty()) {
            nextRace = races.get(races.size() - 1);
        }

        nextRaceLiveData.setValue(nextRace);
    }
}