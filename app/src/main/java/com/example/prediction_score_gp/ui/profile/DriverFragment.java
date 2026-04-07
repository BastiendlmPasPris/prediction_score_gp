package com.example.prediction_score_gp.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prediction_score_gp.R;

public class DriverFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvRole;
    private View userCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userCard   = view.findViewById(R.id.userCard);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail    = view.findViewById(R.id.tvEmail);
        tvRole     = view.findViewById(R.id.tvRole);

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());

        setupWindowInsets(view);
        loadUserData();
        setupHistoryList(view);

        userCard.setAlpha(0f);
        userCard.setTranslationY(20f);
        userCard.animate().alpha(1f).translationY(0f).setDuration(500).start();
    }

    private void setupWindowInsets(View view) {
        View rootLayout   = view.findViewById(R.id.rootLayout);
        View statusSpacer = view.findViewById(R.id.statusBarSpacer);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (statusSpacer != null) {
                ViewGroup.LayoutParams p = statusSpacer.getLayoutParams();
                p.height = sysBars.top;
                statusSpacer.setLayoutParams(p);
            }
            return insets;
        });
    }

    private void loadUserData() {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");
        String email    = prefs.getString("email", "not_connected@f1.com");
        String role     = prefs.getString("role", "MEMBER");

        tvUsername.setText(username);
        tvEmail.setText(email);
        tvRole.setText(role.toUpperCase());

        if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("analyst")) {
            tvRole.setTextColor(Color.parseColor("#FFD700"));
            tvRole.setBackgroundColor(Color.parseColor("#26FFD700"));
        }
    }

    private void setupHistoryList(View view) {
        RecyclerView rvHistory = view.findViewById(R.id.rvHistory);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void logoutUser() {
        Toast.makeText(requireContext(), "Déconnexion en cours...", Toast.LENGTH_SHORT).show();
    }
}
