package com.example.prediction_score_gp.data.model;

public class Prediction {
    private String driver;
    private String race;
    private int predictedPosition;
    private double podiumProbability;
    private String modelVersion;
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
