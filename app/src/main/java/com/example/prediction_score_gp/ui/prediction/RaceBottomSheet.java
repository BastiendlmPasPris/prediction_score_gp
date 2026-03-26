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
import com.example.prediction_score_gp.data.model.Race;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RaceBottomSheet extends BottomSheetDialogFragment {

    // ── Clés Bundle ──────────────────────────────────────────────────
    private static final String ARG_ID      = "id";
    private static final String ARG_FLAG    = "flag";
    private static final String ARG_NAME    = "name";
    private static final String ARG_CIRCUIT = "circuit";
    private static final String ARG_COUNTRY = "country";
    private static final String ARG_DATE    = "date";
    private static final String ARG_SEASON  = "season";

    // ── Callback vers l'Activity ─────────────────────────────────────
    public interface OnPredictListener {
        void onPredict(Race race, String driverName);
    }

    private OnPredictListener predictListener;

    public void setOnPredictListener(OnPredictListener listener) {
        this.predictListener = listener;
    }

    // ── Factory — sérialise la Race dans le Bundle ───────────────────
    public static RaceBottomSheet newInstance(Race race) {
        RaceBottomSheet sheet = new RaceBottomSheet();
        Bundle args = new Bundle();
        args.putInt   (ARG_ID,      race.getId());
        args.putString(ARG_FLAG,    race.getFlagUrl());
        args.putString(ARG_NAME,    race.getName());
        args.putString(ARG_CIRCUIT, race.getCircuit());
        args.putString(ARG_COUNTRY, race.getCountry());
        args.putString(ARG_DATE,    race.getDate());
        args.putInt   (ARG_SEASON,  race.getSeason());
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_race, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();

        // ── Remplir les infos ────────────────────────────────────────
        ((TextView) view.findViewById(R.id.bsFlag))    .setText(args.getString(ARG_FLAG));
        ((TextView) view.findViewById(R.id.bsRaceName)).setText(args.getString(ARG_NAME));
        ((TextView) view.findViewById(R.id.bsCircuit)) .setText(args.getString(ARG_CIRCUIT));
        ((TextView) view.findViewById(R.id.bsDate))    .setText(args.getString(ARG_DATE));

        // Saison dans le champ laps (Race n'a pas laps/distance — on affiche la saison)
        ((TextView) view.findViewById(R.id.bsLaps))    .setText(String.valueOf(args.getInt(ARG_SEASON)));
        // Distance : pays
        ((TextView) view.findViewById(R.id.bsDistance)).setText(args.getString(ARG_COUNTRY));

        // ── Mettre à jour les labels pour correspondre aux champs Race ──
        // (optionnel : renommer les labels dans le XML si vous voulez)
        // Les vues bsLaps et bsDistance affichent Saison et Pays ici

        // ── Spinner pilotes ──────────────────────────────────────────
        Spinner spinner = view.findViewById(R.id.bsSpinnerDriver);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getDriverList()
        ) {
            // Force la couleur du texte de l'item sélectionné
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(android.graphics.Color.parseColor("#CCCCCC"));
                return tv;
            }

            // Force la couleur du texte dans la liste déroulante
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

        // ── Bouton PREDICT ───────────────────────────────────────────
        MaterialButton btnPredict = view.findViewById(R.id.bsBtnPredict);
        btnPredict.setOnClickListener(v -> {
            String selectedDriver = (String) spinner.getSelectedItem();
            if (selectedDriver == null || selectedDriver.equals("— Choisir un pilote —")) {
                Toast.makeText(requireContext(),
                        "Sélectionne un pilote", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reconstruire l'objet Race depuis le Bundle
            Race race = new Race();
            race.setId     (args.getInt   (ARG_ID));
            race.setFlagUrl(args.getString(ARG_FLAG));
            race.setName   (args.getString(ARG_NAME));
            race.setCircuit(args.getString(ARG_CIRCUIT));
            race.setCountry(args.getString(ARG_COUNTRY));
            race.setDate   (args.getString(ARG_DATE));
            race.setSeason (args.getInt   (ARG_SEASON));

            if (predictListener != null) {
                predictListener.onPredict(race, selectedDriver);
            }
            dismiss();
        });
    }

    // ── Liste pilotes ────────────────────────────────────────────────
    // TODO : injecter depuis le ViewModel
    private List<String> getDriverList() {
        return List.of(
                "— Choisir un pilote —",
                "L. Norris",
                "M. Verstappen",
                "O. Piastri",
                "G. Russell",
                "C. Leclerc",
                "L. Hamilton",
                "C. Sainz",
                "F. Alonso",
                "L. Stroll",
                "S. Pérez"
        );
    }
}