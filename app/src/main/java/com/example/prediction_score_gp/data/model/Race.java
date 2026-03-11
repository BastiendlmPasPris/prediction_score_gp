package com.example.prediction_score_gp.data.model;

public class Race {
    private int id;
    private String name;
    private String circuit;
    private String country;
    private String flagUrl;
    private String date;
    private int season;

    public Race() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCircuit() { return circuit; }
    public void setCircuit(String circuit) { this.circuit = circuit; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getFlagUrl() { return flagUrl; }
    public void setFlagUrl(String flagUrl) { this.flagUrl = flagUrl; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }
}
