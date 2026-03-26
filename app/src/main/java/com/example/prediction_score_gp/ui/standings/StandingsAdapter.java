package com.example.prediction_score_gp.ui.standings;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;

import java.util.List;

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.StandingViewHolder> {

    // ── Callback clic ────────────────────────────────────────────────
    public interface OnDriverClickListener {
        void onDriverClick(Driver driver, int position);
    }

    private List<Driver> driverList;
    private final OnDriverClickListener listener;

    public StandingsAdapter(List<Driver> driverList, OnDriverClickListener listener) {
        this.driverList = driverList;
        this.listener   = listener;
    }

    public void updateData(List<Driver> newList) {
        this.driverList = newList;
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

        holder.tvPosition.setText(String.valueOf(position + 1));

        String displayName = driver.getFirstName().charAt(0) + ". " + driver.getLastName();
        holder.tvName.setText(displayName);

        holder.tvTeam.setText(driver.getTeam() != null ? driver.getTeam() : "");

        int probaPercent = (int) Math.round(driver.getPodiumProbability() * 100);
        holder.tvProba.setText(probaPercent + "%");

        holder.imgDriver.setImageResource(android.R.drawable.ic_menu_gallery);
        // TODO : Glide.with(holder.imgDriver).load(driver.getPhotoUrl()).into(holder.imgDriver);

        holder.tvPosition.setTextColor(positionColor(position));

        // ── Clic → bottom sheet ──────────────────────────────────────
        holder.itemView.setOnClickListener(v -> listener.onDriverClick(driver, position));
    }

    @Override
    public int getItemCount() {
        return driverList != null ? driverList.size() : 0;
    }

    private int positionColor(int position) {
        switch (position) {
            case 0:  return Color.parseColor("#FFD700");
            case 1:  return Color.parseColor("#C0C0C0");
            case 2:  return Color.parseColor("#CD7F32");
            default: return Color.parseColor("#888888");
        }
    }

    public static class StandingViewHolder extends RecyclerView.ViewHolder {
        final TextView  tvPosition;
        final TextView  tvName;
        final TextView  tvTeam;
        final TextView  tvProba;
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