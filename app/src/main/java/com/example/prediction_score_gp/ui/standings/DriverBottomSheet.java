package com.example.prediction_score_gp.ui.standings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.ui.prediction.RaceBottomSheet; // Pour les drapeaux !
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DriverBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_FIRST_NAME   = "firstName";
    private static final String ARG_LAST_NAME    = "lastName";
    private static final String ARG_TEAM         = "team";
    private static final String ARG_NATIONALITY  = "nationality";
    private static final String ARG_WINS         = "wins";
    private static final String ARG_PODIUMS      = "podiums";
    private static final String ARG_POLES        = "poles";
    private static final String ARG_PROBA        = "proba";
    private static final String ARG_POSITION     = "position";

    // --- NOUVEAU : On demande la proba et la position explicitement ---
    public static DriverBottomSheet newInstance(Driver driver, double probability, int position) {
        DriverBottomSheet sheet = new DriverBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_FIRST_NAME,  driver.getFirstName());
        args.putString(ARG_LAST_NAME,   driver.getLastName());
        args.putString(ARG_TEAM,        driver.getTeam());
        args.putString(ARG_NATIONALITY, driver.getNationality());
        args.putInt   (ARG_WINS,        driver.getWins());
        args.putInt   (ARG_PODIUMS,     driver.getPodiums());
        args.putInt   (ARG_POLES,       driver.getPoles());

        // On utilise la probabilité envoyée par la prédiction, pas celle du pilote !
        args.putDouble(ARG_PROBA,       probability);
        args.putInt   (ARG_POSITION,    position);

        sheet.setArguments(args);
        return sheet;
    }


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_driver, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        int position = args.getInt(ARG_POSITION);

        // --- Nom ---
        String fullName = args.getString(ARG_FIRST_NAME) + " " + args.getString(ARG_LAST_NAME);
        ((TextView) view.findViewById(R.id.bsDriverName)).setText(fullName);

        // --- Écurie avec sa vraie couleur ! ---
        TextView tvTeam = view.findViewById(R.id.bsDriverTeam);
        String teamName = args.getString(ARG_TEAM);
        tvTeam.setText(teamName);
        tvTeam.setTextColor(StandingsActivity.getTeamColor(teamName)); // Ajout sympa de couleur

        // --- Nationalité (Avec Drapeau !) ---
        String nationality = args.getString(ARG_NATIONALITY);
        String flag = RaceBottomSheet.getFlagEmoji(nationality);
        ((TextView) view.findViewById(R.id.bsDriverNationality))
                .setText(flag + " " + (nationality != null && !nationality.isEmpty() ? nationality : "—"));

        // --- Position ---
        TextView tvPosition = view.findViewById(R.id.bsDriverPosition);
        tvPosition.setText(String.valueOf(position)); // Retrait du "+1" car on passe la vraie place
        tvPosition.setTextColor(positionColor(position));

        // --- Stats ---
        ((TextView) view.findViewById(R.id.bsDriverWins)).setText(String.valueOf(args.getInt(ARG_WINS)));
        ((TextView) view.findViewById(R.id.bsDriverPodiums)).setText(String.valueOf(args.getInt(ARG_PODIUMS)));
        ((TextView) view.findViewById(R.id.bsDriverPoles)).setText(String.valueOf(args.getInt(ARG_POLES)));

        // --- Probabilité ---
        int probaPercent = (int) Math.round(args.getDouble(ARG_PROBA) * 100);
        ((TextView) view.findViewById(R.id.bsProbaValue)).setText(probaPercent + "%");

        ProgressBar pb = view.findViewById(R.id.bsProbaBar);
        if (pb != null) pb.setProgress(probaPercent);

        // --- Photo par défaut ---
        ImageView ivPhoto = view.findViewById(R.id.bsDriverPhoto);
        if (ivPhoto != null) ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    private int positionColor(int position) {
        switch (position) {
            case 1:  return Color.parseColor("#FFD700"); // 1er = Or
            case 2:  return Color.parseColor("#C0C0C0"); // 2ème = Argent
            case 3:  return Color.parseColor("#CD7F32"); // 3ème = Bronze
            default: return Color.parseColor("#888888"); // Reste = Gris
        }
    }
}