package com.example.prediction_score_gp.ui.prediction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;
import com.example.prediction_score_gp.viewmodel.RaceViewModel;
import com.example.prediction_score_gp.viewmodel.SharedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PredictFragment extends Fragment {

    private PredictionViewModel predictionViewModel;
    private RaceViewModel raceViewModel;
    private SharedViewModel sharedViewModel;
    private RaceAdapter raceAdapter;

    private List<Driver> availableDrivers = new ArrayList<>();
    private List<Race> availableRaces = new ArrayList<>();
    private int autoOpenRaceId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_predict, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        raceViewModel       = new ViewModelProvider(this).get(RaceViewModel.class);
        sharedViewModel     = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupWindowInsets(view);
        setupRecyclerView(view);
        setupObservers();

        raceViewModel.loadRaces(2026);
        predictionViewModel.loadDrivers();
    }

    private void setupObservers() {
        predictionViewModel.predictionLiveData.observe(getViewLifecycleOwner(), prediction -> {
            if (prediction != null) {
                String resultat = "Position estimée : " + prediction.getPredictedPosition() +
                        "\nProbabilité Podium : " + String.format("%.1f", prediction.getPodiumProbability() * 100) + "%";
                Toast.makeText(requireContext(), resultat, Toast.LENGTH_LONG).show();
            }
        });

        predictionViewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), "Erreur : " + error, Toast.LENGTH_LONG).show();
        });

        predictionViewModel.loadingLiveData.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading)
                Toast.makeText(requireContext(), "L'IA analyse les données...", Toast.LENGTH_SHORT).show();
        });

        predictionViewModel.driversLiveData.observe(getViewLifecycleOwner(), drivers -> {
            if (drivers != null) {
                this.availableDrivers = drivers;
                tryAutoOpenSheet();
            }
        });

        raceViewModel.racesLiveData.observe(getViewLifecycleOwner(), allRaces -> {
            if (allRaces != null) {
                this.availableRaces = allRaces.stream()
                        .filter(r -> r.getSeason() == 2026)
                        .collect(Collectors.toList());
                raceAdapter.updateRaces(this.availableRaces);
                tryAutoOpenSheet();
            }
        });

        // Observe selectedRaceId from Globe tab
        sharedViewModel.getSelectedRaceId().observe(getViewLifecycleOwner(), raceId -> {
            if (raceId != null && raceId != -1) {
                autoOpenRaceId = raceId;
                tryAutoOpenSheet();
            }
        });
    }

    private void tryAutoOpenSheet() {
        if (autoOpenRaceId != -1 && !availableDrivers.isEmpty() && !availableRaces.isEmpty()) {
            for (Race r : availableRaces) {
                if (r.getId() == autoOpenRaceId) {
                    openRaceSheet(r);
                    autoOpenRaceId = -1;
                    sharedViewModel.setSelectedRaceId(-1);
                    break;
                }
            }
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvRaces = view.findViewById(R.id.rvRaces);
        if (rvRaces == null) return;
        rvRaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        raceAdapter = new RaceAdapter(new ArrayList<>(), this::openRaceSheet);
        rvRaces.setAdapter(raceAdapter);
    }

    private void openRaceSheet(Race race) {
        RaceBottomSheet sheet = RaceBottomSheet.newInstance(race);
        sheet.setDrivers(availableDrivers);
        sheet.setOnPredictListener((selectedRace, driverId) ->
                predictionViewModel.predict(selectedRace.getId(), driverId));
        sheet.show(getChildFragmentManager(), RaceBottomSheet.class.getSimpleName());
    }

    private void setupWindowInsets(View view) {
        View rootLayout   = view.findViewById(R.id.rootLayout);
        View statusSpacer = view.findViewById(R.id.statusBarSpacer);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (statusSpacer != null) {
                statusSpacer.getLayoutParams().height = sysBars.top;
                statusSpacer.requestLayout();
            }
            return insets;
        });
    }
}
