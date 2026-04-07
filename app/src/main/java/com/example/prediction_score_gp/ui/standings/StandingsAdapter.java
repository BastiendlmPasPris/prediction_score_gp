package com.example.prediction_score_gp.ui.standings;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import com.example.prediction_score_gp.data.model.Prediction;
import com.example.prediction_score_gp.ui.prediction.RaceBottomSheet;

import java.util.ArrayList;
import java.util.List;

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.ViewHolder> {

    private List<Prediction> predictions = new ArrayList<>();
    private List<Driver> allDrivers = new ArrayList<>();

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        // Ajout de proba et position
        void onItemClick(Driver driver, double proba, int position);
    }

    public StandingsAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Prediction> newPredictions, List<Driver> drivers) {
        this.predictions = newPredictions;
        this.allDrivers = drivers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_standing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prediction prediction = predictions.get(position);

        int realPosition = position + 4;
        holder.tvPosition.setText(String.valueOf(realPosition));

        holder.tvProba.setText(String.format("%.1f%%", prediction.getPodiumProbability() * 100));

        Driver driver = null;
        String predictedName = prediction.getDriver();

        if (predictedName != null && !predictedName.isEmpty()) {
            String lowerPredictedName = predictedName.toLowerCase();
            for (Driver d : allDrivers) {
                if (d.getLastName() != null && lowerPredictedName.contains(d.getLastName().toLowerCase())) {
                    driver = d;
                    break;
                }
            }
        }

        final Driver finalDriver = driver; // Nécessaire pour le clic

        if (finalDriver != null) {
            String flag = RaceBottomSheet.getFlagEmoji(finalDriver.getNationality());
            holder.tvName.setText(flag + " " + finalDriver.getFirstName().charAt(0) + ". " + finalDriver.getLastName());
            holder.tvTeam.setText(finalDriver.getTeam());
            holder.colorBar.setBackgroundColor(StandingsActivity.getTeamColor(finalDriver.getTeam()));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    // On envoie la probabilité et la vraie position
                    listener.onItemClick(finalDriver, prediction.getPodiumProbability(), realPosition);
                }
            });

        } else {
            holder.tvName.setText(predictedName != null ? predictedName : "Inconnu");
            holder.tvTeam.setText("—");
            holder.colorBar.setBackgroundColor(android.graphics.Color.GRAY);
            holder.itemView.setOnClickListener(null); // Pas de clic si pilote inconnu
        }
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvTeam, tvProba;
        View colorBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.itemPosition);
            tvName = itemView.findViewById(R.id.itemName);
            tvTeam = itemView.findViewById(R.id.itemTeam);
            tvProba = itemView.findViewById(R.id.itemProba);
            colorBar = itemView.findViewById(R.id.itemTeamColor);
        }
    }
}