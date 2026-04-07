package com.example.prediction_score_gp.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Window;
import android.view.WindowManager;

/**
 * Gère le capteur de luminosité ambiante (TYPE_LIGHT).
 * Ajuste automatiquement la luminosité de l'écran en fonction de l'éclairage
 * ambiant, offrant un confort visuel optimal sans intervention de l'utilisateur.
 *
 * Plages :
 *   < 50 lux   → nuit / pièce sombre   → luminosité réduite (0.15)
 *   50–500 lux → éclairage intérieur   → luminosité moyenne  (0.50)
 *   > 500 lux  → lumière du jour       → luminosité maximale (1.00)
 *
 * Usage :
 *   lightSensorHelper = new LightSensorHelper(this, getWindow());
 *   // onResume  → lightSensorHelper.start()
 *   // onPause   → lightSensorHelper.stop()
 */
public class LightSensorHelper {

    private static final float BRIGHTNESS_DARK   = 0.15f;
    private static final float BRIGHTNESS_MEDIUM = 0.50f;
    private static final float BRIGHTNESS_BRIGHT = 1.00f;

    private static final float LUX_DARK_THRESHOLD   = 50f;
    private static final float LUX_BRIGHT_THRESHOLD = 500f;

    private final SensorManager sensorManager;
    private final Sensor         lightSensor;
    private final Window         window;

    private final SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float lux = event.values[0];
            applyBrightness(lux);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { /* ignoré */ }
    };

    /**
     * @param context contexte Android (Activity recommandée)
     * @param window  fenêtre dont la luminosité sera ajustée
     */
    public LightSensorHelper(Context context, Window window) {
        this.window        = window;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.lightSensor   = (sensorManager != null)
                ? sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                : null;
    }

    /** Active l'écoute du capteur. Appeler dans onResume(). */
    public void start() {
        if (sensorManager != null && lightSensor != null) {
            sensorManager.registerListener(listener, lightSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    /** Désactive l'écoute du capteur. Appeler dans onPause(). */
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
        // Restaurer la luminosité système par défaut
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
    }

    /** Retourne vrai si l'appareil possède un capteur de luminosité. */
    public boolean isAvailable() {
        return lightSensor != null;
    }

    // ── privé ─────────────────────────────────────────────────────────────────

    private void applyBrightness(float lux) {
        float brightness;
        if (lux < LUX_DARK_THRESHOLD) {
            brightness = BRIGHTNESS_DARK;
        } else if (lux > LUX_BRIGHT_THRESHOLD) {
            brightness = BRIGHTNESS_BRIGHT;
        } else {
            // Interpolation linéaire entre sombre et lumineux
            float t = (lux - LUX_DARK_THRESHOLD) / (LUX_BRIGHT_THRESHOLD - LUX_DARK_THRESHOLD);
            brightness = BRIGHTNESS_DARK + t * (BRIGHTNESS_BRIGHT - BRIGHTNESS_DARK);
        }
        setWindowBrightness(brightness);
    }

    private void setWindowBrightness(float brightness) {
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = brightness;
        window.setAttributes(params);
    }
}
