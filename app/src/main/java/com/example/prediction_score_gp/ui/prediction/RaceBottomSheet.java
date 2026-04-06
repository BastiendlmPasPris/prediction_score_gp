package com.example.prediction_score_gp.ui.prediction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Race;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RaceBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_FLAG = "flag";
    private static final String ARG_NAME = "name";
    private static final String ARG_CIRCUIT = "circuit";
    private static final String ARG_COUNTRY = "country";
    private static final String ARG_DATE = "date";
    private static final String ARG_SEASON = "season";

    private OnPredictListener predictListener;
    private List<Driver> apiDrivers = new ArrayList<>(); // <--- AJOUTÉ

    public interface OnPredictListener {
        void onPredict(Race race, int driverId);
    }

    public void setOnPredictListener(OnPredictListener listener) {
        this.predictListener = listener;
    }

    // <--- NOUVEAU : Setter pour recevoir les pilotes depuis l'Activity
    public void setDrivers(List<Driver> drivers) {
        this.apiDrivers = drivers;
    }

    public static RaceBottomSheet newInstance(Race race) {
        RaceBottomSheet sheet = new RaceBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, race.getId());
        args.putString(ARG_FLAG, race.getFlagUrl());
        args.putString(ARG_NAME, race.getName());
        args.putString(ARG_CIRCUIT, race.getCircuit());
        args.putString(ARG_COUNTRY, race.getCountry());
        args.putString(ARG_DATE, race.getDate());
        args.putInt(ARG_SEASON, race.getSeason());
        sheet.setArguments(args);
        return sheet;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_race, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();

        ((TextView) view.findViewById(R.id.bsFlag)).setText(args.getString(ARG_FLAG));
        ((TextView) view.findViewById(R.id.bsRaceName)).setText(args.getString(ARG_NAME));
        ((TextView) view.findViewById(R.id.bsCircuit)).setText(args.getString(ARG_CIRCUIT));
        ((TextView) view.findViewById(R.id.bsDate)).setText(args.getString(ARG_DATE));
        ((TextView) view.findViewById(R.id.bsLaps)).setText(String.valueOf(args.getInt(ARG_SEASON)));
        ((TextView) view.findViewById(R.id.bsDistance)).setText(args.getString(ARG_COUNTRY));

        Spinner spinner = view.findViewById(R.id.bsSpinnerDriver);

        // --- GÉNÉRATION DYNAMIQUE DE LA LISTE DES PILOTES ---
        List<DriverOption> options = new ArrayList<>();
        options.add(new DriverOption(-1, "— Choose a pilot —"));

        // Filtre pour ne garder que la grille officielle de la saison 2026
        List<Integer> grid2026Ids = Arrays.asList(830, 1, 844, 846, 857, 847, 832, 4, 840, 815, 848, 866, 839, 822, 842, 807, 860,863, 872, 859, 861, 876);

        if (apiDrivers != null) {
            for (Driver d : apiDrivers) {
                if (grid2026Ids.contains(d.getId())) {
                    String initial = d.getFirstName() != null && !d.getFirstName().isEmpty() ? d.getFirstName().substring(0, 1) + ". " : "";
                    String team = d.getTeam() != null ? d.getTeam() : "Unknown";

                    // flag
                    String flag = getFlagEmoji(d.getNationality());

                    // On assemble le texte avec l'émoji au début
                    String displayName = flag + " " + initial + d.getLastName() + " (" + team + ")";

                    options.add(new DriverOption(d.getId(), displayName));
                }
            }
        }

        // Trier la liste alphabétiquement (en gardant "Choisir" en premier)
        options.sort((o1, o2) -> {
            if (o1.id == -1) return -1;
            if (o2.id == -1) return 1;
            return o1.name.compareToIgnoreCase(o2.name);
        });

        ArrayAdapter<DriverOption> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                options
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.parseColor("#CCCCCC"));
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.parseColor("#CCCCCC"));
                tv.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"));
                tv.setPadding(32, 0, 32, 0);
                return tv;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        MaterialButton btnPredict = view.findViewById(R.id.bsBtnPredict);
        btnPredict.setOnClickListener(v -> {
            DriverOption selectedDriver = (DriverOption) spinner.getSelectedItem();

            if (selectedDriver == null || selectedDriver.id == -1) {
                Toast.makeText(requireContext(), "Sélectionne un pilote", Toast.LENGTH_SHORT).show();
                return;
            }

            Race race = new Race();
            race.setId(args.getInt(ARG_ID));
            race.setFlagUrl(args.getString(ARG_FLAG));
            race.setName(args.getString(ARG_NAME));
            race.setCircuit(args.getString(ARG_CIRCUIT));
            race.setCountry(args.getString(ARG_COUNTRY));
            race.setDate(args.getString(ARG_DATE));
            race.setSeason(args.getInt(ARG_SEASON));

            if (predictListener != null) {
                predictListener.onPredict(race, selectedDriver.id);
            }
            dismiss();
        });
    }

    private static class DriverOption {
        int id;
        String name;

        DriverOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
    // --- MÉTHODE POUR CONVERTIR LE TEXTE EN EMOJI DRAPEAU ---
    // --- MÉTHODE INTELLIGENTE POUR LES DRAPEAUX ---
    public static String getFlagEmoji(String text) {
        if (text == null || text.isEmpty()) return "🏁";

        String lower = text.toLowerCase();

        if (lower.contains("british") || lower.contains("uk") || lower.contains("great britain")) return "🇬🇧";
        if (lower.contains("dutch") || lower.contains("netherlands")) return "🇳🇱";
        if (lower.contains("monegasque") || lower.contains("monaco")) return "🇲🇨";
        if (lower.contains("spanish") || lower.contains("spain") || lower.contains("barcelona") || lower.contains("madrid")) return "🇪🇸";
        if (lower.contains("australian") || lower.contains("australia")) return "🇦🇺";
        if (lower.contains("mexican") || lower.contains("mexico")) return "🇲🇽";
        if (lower.contains("canadian") || lower.contains("canada")) return "🇨🇦";
        if (lower.contains("japanese") || lower.contains("japan")) return "🇯🇵";
        if (lower.contains("chinese") || lower.contains("china")) return "🇨🇳";
        if (lower.contains("american") || lower.contains("usa") || lower.contains("united states") || lower.contains("miami") || lower.contains("las vegas")) return "🇺🇸";
        if (lower.contains("german") || lower.contains("germany")) return "🇩🇪";
        if (lower.contains("finnish") || lower.contains("finland")) return "🇫🇮";
        if (lower.contains("danish") || lower.contains("denmark")) return "🇩🇰";
        if (lower.contains("thai") || lower.contains("thailand")) return "🇹🇭";
        if (lower.contains("zealander") || lower.contains("new zealand")) return "🇳🇿";
        if (lower.contains("italian") || lower.contains("italy") || lower.contains("emilia")) return "🇮🇹";
        if (lower.contains("brazilian") || lower.contains("brazil") || lower.contains("são paulo")) return "🇧🇷";
        if (lower.contains("austrian") || lower.contains("austria")) return "🇦🇹";
        if (lower.contains("belgian") || lower.contains("belgium")) return "🇧🇪";
        if (lower.contains("hungarian") || lower.contains("hungary")) return "🇭🇺";
        if (lower.contains("azerbaijan")) return "🇦🇿";
        if (lower.contains("singapore")) return "🇸🇬";
        if (lower.contains("qatar")) return "🇶🇦";
        if (lower.contains("uae") || lower.contains("abu dhabi") || lower.contains("emirates")) return "🇦🇪";
        if (lower.contains("saudi")) return "🇸🇦";
        if (lower.contains("bahrain")) return "🇧🇭";
        if (lower.contains("french") || lower.contains("france")) return "🇫🇷";

        return "🏁"; // Par défaut
    }
}