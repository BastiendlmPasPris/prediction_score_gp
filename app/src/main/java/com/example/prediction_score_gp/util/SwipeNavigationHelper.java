package com.example.prediction_score_gp.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Ajoute la navigation par swipe gauche/droite entre les onglets principaux.
 * L'ordre des onglets est : Globe(0) → Predict(1) → Podium(2) → Driver/Profile(3).
 *
 * Usage :
 *   SwipeNavigationHelper.attach(rootView, this::goToPreviousTab, this::goToNextTab);
 */
public class SwipeNavigationHelper {

    private static final int SWIPE_THRESHOLD   = 120; // px minimaux pour déclencher le swipe
    private static final int SWIPE_VELOCITY    = 100; // vitesse minimale (px/s)

    public interface OnSwipeCallback {
        void onSwipeLeft();   // vers l'onglet suivant (index +1)
        void onSwipeRight();  // vers l'onglet précédent (index -1)
    }

    /**
     * Attache la détection de swipe à la vue donnée.
     *
     * @param view     vue racine de l'Activity (ex. rootLayout)
     * @param callback callback appelé lors d'un swipe valide
     */
    public static void attach(View view, OnSwipeCallback callback) {
        Context context = view.getContext();

        GestureDetector detector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        // Nécessaire pour que onFling soit déclenché
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                          float velocityX, float velocityY) {
                        if (e1 == null || e2 == null) return false;

                        float dx = e2.getX() - e1.getX();
                        float dy = e2.getY() - e1.getY();

                        // Le swipe doit être majoritairement horizontal
                        if (Math.abs(dx) <= Math.abs(dy)) return false;
                        if (Math.abs(dx) < SWIPE_THRESHOLD) return false;
                        if (Math.abs(velocityX) < SWIPE_VELOCITY) return false;

                        if (dx < 0) {
                            callback.onSwipeLeft();   // glissement vers la gauche → onglet suivant
                        } else {
                            callback.onSwipeRight();  // glissement vers la droite → onglet précédent
                        }
                        return true;
                    }
                });

        // Intercepte les touch events sur la vue
        view.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false; // ne consomme pas l'événement pour ne pas bloquer les clics enfants
        });
    }
}
