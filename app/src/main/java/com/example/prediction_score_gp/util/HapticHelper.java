package com.example.prediction_score_gp.util;

import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Utilitaire de retour haptique (vibration tactile).
 * Utilise performHapticFeedback() — aucune permission requise.
 *
 * Usage :
 *   HapticHelper.tap(view);         // clic léger (bouton, tab)
 *   HapticHelper.confirm(view);     // action confirmée (prédiction, sauvegarde)
 *   HapticHelper.longPress(view);   // appui long
 */
public class HapticHelper {

    /**
     * Vibration légère pour un appui simple (bouton, onglet).
     */
    public static void tap(View view) {
        if (view == null) return;
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    /**
     * Vibration plus forte pour une action confirmée (résultat de prédiction, sauvegarde).
     */
    public static void confirm(View view) {
        if (view == null) return;
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    /**
     * Vibration pour un appui long.
     */
    public static void longPress(View view) {
        if (view == null) return;
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }
}
