package com.example.prediction_score_gp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    private static final int[] TAB_IDS       = {R.id.tabGlobe,   R.id.tabPredict,  R.id.tabPodium,  R.id.tabDriver};
    private static final int[] ICON_IDS      = {R.id.iconGlobe,  R.id.iconPredict, R.id.iconPodium, R.id.iconDriver};
    private static final int[] LABEL_IDS     = {R.id.labelGlobe, R.id.labelPredict,R.id.labelPodium,R.id.labelDriver};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new TabAdapter(this));
        // Keep all fragments alive to avoid reloading WebView when switching tabs
        viewPager.setOffscreenPageLimit(3);
        // Disable swipe on Globe tab (pos 0) to avoid WebView touch conflict
        viewPager.setUserInputEnabled(false);

        setupNavBar();
        setupWindowInsets();
    }

    public void switchToTab(int position) {
        viewPager.setCurrentItem(position, true);
    }

    private void setupNavBar() {
        int activeColor   = Color.parseColor("#FF3030");
        int inactiveColor = Color.parseColor("#888888");

        // Tab click listeners
        for (int i = 0; i < TAB_IDS.length; i++) {
            final int index = i;
            View tab = findViewById(TAB_IDS[i]);
            if (tab != null) {
                tab.setOnClickListener(v -> viewPager.setCurrentItem(index, true));
            }
        }

        // Update nav bar when page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Enable swipe only on non-Globe tabs
                viewPager.setUserInputEnabled(position != 0);

                for (int i = 0; i < ICON_IDS.length; i++) {
                    boolean active = (i == position);
                    ImageView icon = findViewById(ICON_IDS[i]);
                    TextView label = findViewById(LABEL_IDS[i]);
                    if (icon != null) icon.setColorFilter(active ? activeColor : inactiveColor);
                    if (label != null) label.setTextColor(active ? activeColor : inactiveColor);
                }
            }
        });
    }

    private void setupWindowInsets() {
        View navBarSpacer = findViewById(R.id.navBarSpacer);
        ViewCompat.setOnApplyWindowInsetsListener(navBarSpacer, (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams p = v.getLayoutParams();
            p.height = sysBars.bottom;
            v.setLayoutParams(p);
            return insets;
        });
    }
}
