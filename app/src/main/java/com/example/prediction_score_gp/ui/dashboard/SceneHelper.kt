package com.example.prediction_score_gp.ui.dashboard

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.utils.Manipulator
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.CameraGestureDetector

import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

object SceneHelper {

    @JvmStatic
    fun loadModel(sceneView: SceneView, lifecycleOwner: LifecycleOwner) {

        // Lumière principale
        sceneView.mainLightNode?.intensity = 100_000f



        // Position initiale dézoomée
        val startPosition = Position(z = 2.0f)
        val targetPosition = Position(0f, 0f, 0f)

        sceneView.cameraNode.position = startPosition
        sceneView.cameraNode.lookAt(targetPosition)

        // Manipulateur avec la bonne position de départ — même API que l'exemple officiel
        sceneView.cameraManipulator = object : CameraGestureDetector.DefaultCameraManipulator(
            orbitHomePosition = startPosition,
            targetPosition = targetPosition
        ) {
            override fun grabUpdate(x: Int, y: Int) {
                // Forcer y à rester fixe = bloquer rotation verticale
                super.grabUpdate(x, 0)
            }
        }

        val modelNode = ModelNode(
            modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = "earth2.glb"
            ),
            scaleToUnits = 1.0f
        )

        sceneView.childNodes.toMutableList().also {
            it.add(modelNode)
            sceneView.childNodes = it
        }


        Log.d("SceneView", "Modèle chargé !")
    }
}