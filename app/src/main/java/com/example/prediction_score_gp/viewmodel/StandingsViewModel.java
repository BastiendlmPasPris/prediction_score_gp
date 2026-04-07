package com.example.prediction_score_gp.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prediction_score_gp.data.api.DriversApi;
import com.example.prediction_score_gp.data.api.RetrofitClient;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.data.repository.PredictRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StandingsViewModel extends ViewModel {
    public MutableLiveData<List<Driver>> predictedStandings = new MutableLiveData<>();

    public MutableLiveData<String> raceName = new MutableLiveData<>();
    private PredictRepository predictRepository = new PredictRepository();

    // On récupère votre interface DriversApi via le RetrofitClient
    private DriversApi driversApi = RetrofitClient.getInstance().create(DriversApi.class);

    private final List<Integer> driverIds = Arrays.asList(830, 844, 1, 846, 857, 847, 832, 4);

    public void loadPredictions(int raceId) {
        List<Driver> results = new ArrayList<>();

        for (Integer id : driverIds) {
            // 1er APPEL : On demande la probabilité à l'IA
            predictRepository.predict(raceId, id).enqueue(new Callback<Prediction>() {
                @Override
                public void onResponse(@NonNull Call<Prediction> call, @NonNull Response<Prediction> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Prediction p = response.body();

                        if (raceName.getValue() == null) {
                            raceName.postValue(p.getRace());
                        }

                        driversApi.getDriverById(id).enqueue(new Callback<Driver>() {
                            @Override
                            public void onResponse(@NonNull Call<Driver> call, @NonNull Response<Driver> driverResponse) {
                                Driver d = driverResponse.body();

                                // Sécurité : si l'API Driver ne trouve pas le pilote, on crée un pilote vide
                                if (d == null) {
                                    d = new Driver();
                                    d.setId(id);
                                    d.setLastName(p.getDriver()); // On utilise au moins le nom de l'IA
                                }

                                // On injecte la probabilité de podium calculée par l'IA
                                d.setPodiumProbability(p.getPodiumProbability());

                                // --- AJOUT DU PALMARÈS POUR ENLEVER LES ZÉROS ---
                                enrichDriverStats(d);

                                // Ajout à la liste
                                results.add(d);
                                Log.d("F1_PREDICT", "Pilote complet reçu : " + d.getLastName() + " - Total : " + results.size());

                                // Mise à jour de l'écran en temps réel
                                List<Driver> currentList = new ArrayList<>(results);
                                currentList.sort((d1, d2) -> Double.compare(d2.getPodiumProbability(), d1.getPodiumProbability()));
                                predictedStandings.postValue(currentList);
                            }

                            @Override
                            public void onFailure(@NonNull Call<Driver> call, @NonNull Throwable t) {
                                Log.e("F1_PREDICT", "Erreur lors de la récupération des stats du pilote " + id, t);
                            }
                        });

                    } else {
                        Log.e("F1_PREDICT", "Erreur API Prédiction pour le pilote " + id);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Prediction> call, @NonNull Throwable t) {
                    Log.e("F1_PREDICT", "Échec réseau pour la prédiction du pilote " + id, t);
                }
            });
        }
    }


    private void enrichDriverStats(Driver d) {
        switch (d.getId()) {
            case 830: // Max Verstappen
                d.setWins(62);
                d.setPodiums(111);
                d.setPoles(40);
                break;

            case 1: // Lewis Hamilton
                d.setWins(105);
                d.setPodiums(201);
                d.setPoles(104);
                break;

            case 4: // Fernando Alonso
                d.setWins(32);
                d.setPodiums(106);
                d.setPoles(22);
                break;

            case 844: // Charles Leclerc
                d.setWins(8);
                d.setPodiums(41);
                d.setPoles(26);
                break;

            case 832: // Carlos Sainz
                d.setWins(4);
                d.setPodiums(25);
                d.setPoles(6);
                break;

            case 846: // Lando Norris
                d.setWins(3);
                d.setPodiums(25);
                d.setPoles(8);
                break;

            case 847: // George Russell
                d.setWins(2);
                d.setPodiums(14);
                d.setPoles(3);
                break;

            case 857: // Oscar Piastri
                d.setWins(2);
                d.setPodiums(9);
                d.setPoles(0);
                break;

            default:
                // Pour les autres pilotes, on laisse à 0 ou on cherche une autre source
                break;
        }
    }
}