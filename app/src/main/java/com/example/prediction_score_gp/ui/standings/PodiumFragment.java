package com.example.prediction_score_gp.ui.standings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.data.model.Race;
import com.example.prediction_score_gp.ui.prediction.RaceBottomSheet;
import com.example.prediction_score_gp.viewmodel.PredictionViewModel;
import com.example.prediction_score_gp.viewmodel.RaceViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PodiumFragment extends Fragment {

    private PredictionViewModel predictionViewModel;
    private RaceViewModel raceViewModel;
    private StandingsAdapter adapter;

    private List<Driver> allDrivers = new ArrayList<>();
    private Spinner spinnerRace;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_podium, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        predictionViewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        raceViewModel       = new ViewModelProvider(this).get(RaceViewModel.class);

        setupWindowInsets(view);
        setupRecyclerView(view);

        spinnerRace = view.findViewById(R.id.spinnerRace);

        predictionViewModel.loadDrivers();
        raceViewModel.loadRaces(2026);

        setupObservers(view);
    }

    private void setupObservers(View view) {
        predictionViewModel.driversLiveData.observe(getViewLifecycleOwner(), drivers -> {
            if (drivers != null) allDrivers = drivers;
        });

        raceViewModel.racesLiveData.observe(getViewLifecycleOwner(), races -> {
            if (races != null && !races.isEmpty()) {
                List<Race> races2026 = races.stream().filter(r -> r.getSeason() == 2026).collect(Collectors.toList());
                setupRaceSpinner(races2026);
            }
        });

        predictionViewModel.standingsLiveData.observe(getViewLifecycleOwner(), rawPredictions -> {
            if (rawPredictions != null && !allDrivers.isEmpty()) {
                updatePodium(view, rawPredictions);
            }
        });

        predictionViewModel.loadingLiveData.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) Toast.makeText(requireContext(), "L'IA simule la course...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRaceSpinner(List<Race> races) {
        List<RaceOption> options = new ArrayList<>();
        options.add(new RaceOption(-1, "— Sélectionner un Grand Prix —"));
        for (Race r : races) {
            String flag = RaceBottomSheet.getFlagEmoji(r.getCountry());
            options.add(new RaceOption(r.getId(), flag + " " + r.getName()));
        }

        ArrayAdapter<RaceOption> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.parseColor("#E0E0E0"));
                tv.setPadding(32, 16, 32, 16);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#1A1A1A"));
                tv.setPadding(32, 32, 32, 32);
                return tv;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRace.setAdapter(spinnerAdapter);
        spinnerRace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                RaceOption selected = (RaceOption) parent.getItemAtPosition(position);
                if (selected.id != -1) predictionViewModel.predictFullRace(selected.id);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePodium(View view, List<Prediction> rawPredictions) {
        List<Integer> grid2026Ids = Arrays.asList(830, 1, 844, 846, 857, 847, 832, 4, 840, 815, 848, 866, 839, 822, 842, 807, 860, 863, 872, 859, 861, 876);

        List<Prediction> predictions = new ArrayList<>();
        for (Prediction p : rawPredictions) {
            Driver d = findDriverByName(p.getDriver());
            if (d != null && grid2026Ids.contains(d.getId())) {
                predictions.add(p);
            }
            if (predictions.size() == 22) break;
        }

        if (predictions.size() < 3) return;

        Driver p1 = findDriverByName(predictions.get(0).getDriver());
        Driver p2 = findDriverByName(predictions.get(1).getDriver());
        Driver p3 = findDriverByName(predictions.get(2).getDriver());

        if (p1 != null) {
            ((TextView) view.findViewById(R.id.tvP1Name)).setText(p1.getLastName());
            view.findViewById(R.id.colorP1).setBackgroundColor(StandingsActivity.getTeamColor(p1.getTeam()));
            view.findViewById(R.id.cardP1).setOnClickListener(v -> openDriverSheet(p1, predictions.get(0).getPodiumProbability(), 1));
        }
        if (p2 != null) {
            ((TextView) view.findViewById(R.id.tvP2Name)).setText(p2.getLastName());
            view.findViewById(R.id.colorP2).setBackgroundColor(StandingsActivity.getTeamColor(p2.getTeam()));
            view.findViewById(R.id.cardP2).setOnClickListener(v -> openDriverSheet(p2, predictions.get(1).getPodiumProbability(), 2));
        }
        if (p3 != null) {
            ((TextView) view.findViewById(R.id.tvP3Name)).setText(p3.getLastName());
            view.findViewById(R.id.colorP3).setBackgroundColor(StandingsActivity.getTeamColor(p3.getTeam()));
            view.findViewById(R.id.cardP3).setOnClickListener(v -> openDriverSheet(p3, predictions.get(2).getPodiumProbability(), 3));
        }

        if (predictions.size() > 3) {
            adapter.updateData(predictions.subList(3, predictions.size()), allDrivers);
        }
    }

    private void openDriverSheet(Driver driver, double proba, int position) {
        DriverBottomSheet sheet = DriverBottomSheet.newInstance(driver, proba, position);
        sheet.show(getChildFragmentManager(), DriverBottomSheet.class.getSimpleName());
    }

    private Driver findDriverByName(String predictedName) {
        if (predictedName == null || predictedName.isEmpty()) return null;
        String lower = predictedName.toLowerCase();
        for (Driver d : allDrivers) {
            if (d.getLastName() != null && lower.contains(d.getLastName().toLowerCase())) return d;
        }
        return null;
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvStandings = view.findViewById(R.id.rvStandings);
        rvStandings.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StandingsAdapter(this::openDriverSheet);
        rvStandings.setAdapter(adapter);
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

    private static class RaceOption {
        int id; String name;
        RaceOption(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
