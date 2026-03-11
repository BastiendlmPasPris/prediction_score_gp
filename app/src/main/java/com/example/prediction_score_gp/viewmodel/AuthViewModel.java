package com.example.prediction_score_gp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prediction_score_gp.data.model.User;
import com.example.prediction_score_gp.data.repository.AuthRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    public MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        repository.login(email, password).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    userLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Email ou mot de passe incorrect");
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public void register(String email, String password, String username) {
        loadingLiveData.setValue(true);
        repository.register(email, password, username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    userLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Erreur lors de l'inscription");
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }
}
