package com.example.prediction_score_gp.data.model;

public class Driver {
    private int id;
    private String firstName;
    private String lastName;
    private String nationality;
    private String team;
    private String photoUrl;
    private int wins;
    private int podiums;
    private int poles;
    private double podiumProbability; // utilisé pour le classement prédit

    public Driver() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getPodiums() { return podiums; }
    public void setPodiums(int podiums) { this.podiums = podiums; }

    public int getPoles() { return poles; }
    public void setPoles(int poles) { this.poles = poles; }

    public double getPodiumProbability() { return podiumProbability; }
    public void setPodiumProbability(double podiumProbability) { this.podiumProbability = podiumProbability; }
}
