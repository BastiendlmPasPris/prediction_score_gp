package com.example.prediction_score_gp.data.model;

import com.google.gson.annotations.SerializedName;

public class Prediction {

    @SerializedName("driver")
    private String driver;

    @SerializedName("race")
    private String race;

    @SerializedName("predicted_position")
    private int predictedPosition;

    @SerializedName("podium_probability")
    private double podiumProbability;

    @SerializedName("model_version")
    private String modelVersion;

    @SerializedName("confidence_interval")
    private int[] confidenceInterval; // [min, max]

    public Prediction() {}

    // Getters & Setters
    public String getDriver() { return driver; }
    public void setDriver(String driver) { this.driver = driver; }

    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }

    public int getPredictedPosition() { return predictedPosition; }
    public void setPredictedPosition(int predictedPosition) { this.predictedPosition = predictedPosition; }

    public double getPodiumProbability() { return podiumProbability; }
    public void setPodiumProbability(double podiumProbability) { this.podiumProbability = podiumProbability; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public int[] getConfidenceInterval() { return confidenceInterval; }
    public void setConfidenceInterval(int[] confidenceInterval) { this.confidenceInterval = confidenceInterval; }
}