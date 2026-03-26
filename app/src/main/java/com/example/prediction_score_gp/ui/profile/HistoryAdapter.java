package com.example.prediction_score_gp.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;
import com.example.prediction_score_gp.data.model.Prediction;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Prediction> predictions = new ArrayList<>();

    public void updateData(List<Prediction> newList) {
        this.predictions = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prediction p = predictions.get(position);
        holder.text1.setText(p.getDriver() + " — " + p.getRace());
        holder.text2.setText("Position: " + p.getPredictedPosition()
                + "  |  Podium: " + Math.round(p.getPodiumProbability() * 100) + "%");
    }

    @Override
    public int getItemCount() { return predictions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text1, text2;
        ViewHolder(View v) {
            super(v);
            text1 = v.findViewById(android.R.id.text1);
            text2 = v.findViewById(android.R.id.text2);
        }
    }
}
