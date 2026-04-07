package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedRaceId = new MutableLiveData<>(-1);

    public void setSelectedRaceId(int id) {
        selectedRaceId.setValue(id);
    }

    public LiveData<Integer> getSelectedRaceId() {
        return selectedRaceId;
    }
}
