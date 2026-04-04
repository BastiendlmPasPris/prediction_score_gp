package com.example.prediction_score_gp.ui.prediction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Race;

import java.util.ArrayList;
import java.util.List;

public class RaceAdapter extends RecyclerView.Adapter<RaceAdapter.ViewHolder> {

    public interface OnRaceClickListener {
        void onRaceClick(Race race);
    }

    // On utilise une liste modifiable pour pouvoir la rafraîchir avec l'API
    private final List<Race> races;
    private final OnRaceClickListener listener;

    public RaceAdapter(List<Race> initialRaces, OnRaceClickListener listener) {
        // On copie dans une nouvelle ArrayList pour éviter les crashs (UnsupportedOperationException)
        // si initialRaces est une liste verrouillée (comme List.of(...))
        this.races = new ArrayList<>(initialRaces);
        this.listener = listener;
    }

    // ── NOUVELLE MÉTHODE : Mettre à jour la liste depuis l'API ──────
    public void updateRaces(List<Race> newRaces) {
        this.races.clear();
        if (newRaces != null) {
            this.races.addAll(newRaces);
        }
        notifyDataSetChanged(); // Demande à l'écran de se redessiner avec les nouvelles données
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_race, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Race race = races.get(position);

        // flagUrl contient soit une URL soit un emoji selon votre source de données
        holder.tvFlag.setText(race.getFlagUrl());
        holder.tvName.setText(race.getName());

        holder.itemView.setOnClickListener(v -> listener.onRaceClick(race));
    }

    @Override
    public int getItemCount() {
        return races != null ? races.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvFlag, tvName;
        ViewHolder(View v) {
            super(v);
            tvFlag = v.findViewById(R.id.tvFlag);
            tvName = v.findViewById(R.id.tvRaceName);
        }
    }

    // ── Données exemple ──────────────────────────────────────────────
    // (Vous pourrez supprimer cette méthode quand l'API gèrera tout)
    public static List<Race> getSampleRaces() {
        Race r1 = new Race();
        r1.setId(1); r1.setName("GP of Australia");
        r1.setCircuit("Albert Park Circuit");
        r1.setCountry("Australia"); r1.setFlagUrl("🇦🇺");
        r1.setDate("30 Mar 2025"); r1.setSeason(2025);

        Race r2 = new Race();
        r2.setId(2); r2.setName("GP of China");
        r2.setCircuit("Shanghai International");
        r2.setCountry("China"); r2.setFlagUrl("🇨🇳");
        r2.setDate("13 Apr 2025"); r2.setSeason(2025);

        Race r3 = new Race();
        r3.setId(3); r3.setName("GP of Japan");
        r3.setCircuit("Suzuka Circuit");
        r3.setCountry("Japan"); r3.setFlagUrl("🇯🇵");
        r3.setDate("6 Apr 2025"); r3.setSeason(2025);

        Race r4 = new Race();
        r4.setId(4); r4.setName("GP of Bahrein");
        r4.setCircuit("Bahrain International");
        r4.setCountry("Bahrain"); r4.setFlagUrl("🇧🇭");
        r4.setDate("20 Apr 2025"); r4.setSeason(2025);

        Race r5 = new Race();
        r5.setId(5); r5.setName("Saudi Arabian GP");
        r5.setCircuit("Jeddah Corniche Circuit");
        r5.setCountry("Saudi Arabia"); r5.setFlagUrl("🇸🇦");
        r5.setDate("27 Apr 2025"); r5.setSeason(2025);

        Race r6 = new Race();
        r6.setId(6); r6.setName("GP of Miami");
        r6.setCircuit("Miami International");
        r6.setCountry("USA"); r6.setFlagUrl("🇺🇸");
        r6.setDate("4 May 2025"); r6.setSeason(2025);

        return List.of(r1, r2, r3, r4, r5, r6);
    }
}