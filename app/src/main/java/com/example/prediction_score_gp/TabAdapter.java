package com.example.prediction_score_gp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.prediction_score_gp.ui.dashboard.GlobeFragment;
import com.example.prediction_score_gp.ui.prediction.PredictFragment;
import com.example.prediction_score_gp.ui.standings.PodiumFragment;
import com.example.prediction_score_gp.ui.profile.DriverFragment;

public class TabAdapter extends FragmentStateAdapter {

    public TabAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new GlobeFragment();
            case 1: return new PredictFragment();
            case 2: return new PodiumFragment();
            case 3: return new DriverFragment();
            default: throw new IllegalStateException("Invalid tab position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
