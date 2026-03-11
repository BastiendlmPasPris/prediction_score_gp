package com.example.prediction_score_gp.ui.standings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Driver;
import java.util.List;

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.StandingViewHolder> {

    private List<Driver> driverList;

    public StandingsAdapter(List<Driver> driverList) {
        this.driverList = driverList;
    }

    @NonNull
    @Override
    public StandingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_standing, parent, false);
        return new StandingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingViewHolder holder, int position) {
        // TODO: Lier les données du pilote à la vue
        // TODO: Gérer la couleur de fond (or/argent/bronze pour top 3)
    }

    @Override
    public int getItemCount() {
        return driverList != null ? driverList.size() : 0;
    }

    public static class StandingViewHolder extends RecyclerView.ViewHolder {
        // TODO: Déclarer les vues (ImageView photo, TextView nom, TextView écurie, TextView position)
        public StandingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
