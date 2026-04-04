package com.example.prediction_score_gp.ui.standings;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Assurez-vous d'avoir importé Glide dans build.gradle si vous l'utilisez
// import com.bumptech.glide.Glide;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;

import java.util.List;

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.StandingViewHolder> {

    public interface OnDriverClickListener {
        void onDriverClick(Driver driver, int position);
    }

    private final List<Driver> driverList;
    private final OnDriverClickListener listener;

    public StandingsAdapter(List<Driver> driverList, OnDriverClickListener listener) {
        this.driverList = driverList;
        this.listener   = listener;
    }

    public void updateData(List<Driver> newDrivers) {
        this.driverList.clear();
        if (newDrivers != null) {
            this.driverList.addAll(newDrivers);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public StandingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_standing, parent, false);
        return new StandingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingViewHolder holder, int position) {
        Driver driver = driverList.get(position);

        // 1. Position et Couleurs (Or, Argent, Bronze)
        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvPosition.setTextColor(positionColor(position));

        // 2. Nom formaté (Ex: "L. Norris")
        String firstName = driver.getFirstName() != null ? driver.getFirstName() : "";
        String initial = (!firstName.isEmpty()) ? firstName.substring(0, 1) + ". " : "";
        String lastName = driver.getLastName() != null ? driver.getLastName() : "Inconnu";
        holder.tvName.setText(initial + lastName);

        // 3. Écurie
        holder.tvTeam.setText(driver.getTeam() != null ? driver.getTeam() : "Non assignée");

        // 4. Probabilité en pourcentage
        int probaPercent = (int) Math.round(driver.getPodiumProbability() * 100);
        holder.tvProba.setText(probaPercent + "%");

        // 5. Photo (Placeholder par défaut, décommentez Glide quand installé)
        holder.imgDriver.setImageResource(android.R.drawable.ic_menu_gallery);
        /*
        if (driver.getPhotoUrl() != null && !driver.getPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                 .load(driver.getPhotoUrl())
                 .circleCrop() // Pour une image ronde
                 .into(holder.imgDriver);
        }
        */

        // 6. Clic
        holder.itemView.setOnClickListener(v -> listener.onDriverClick(driver, position));
    }

    @Override
    public int getItemCount() {
        return driverList != null ? driverList.size() : 0;
    }

    private int positionColor(int position) {
        switch (position) {
            case 0:  return Color.parseColor("#FFD700"); // Or
            case 1:  return Color.parseColor("#C0C0C0"); // Argent
            case 2:  return Color.parseColor("#CD7F32"); // Bronze
            default: return Color.parseColor("#888888"); // Gris normal
        }
    }

    public static class StandingViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPosition, tvName, tvTeam, tvProba;
        final ImageView imgDriver;

        public StandingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvName     = itemView.findViewById(R.id.tvDriverName);
            tvTeam     = itemView.findViewById(R.id.tvTeam);
            tvProba    = itemView.findViewById(R.id.tvPodiumProba);
            imgDriver  = itemView.findViewById(R.id.imgDriver);
        }
    }
}