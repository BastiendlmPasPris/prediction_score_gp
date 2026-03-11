package com.example.prediction_score_gp.data.model;

public class User {
    private int id;
    private String email;
    private String username;
    private String role; // "user" ou "admin"
    private String token; // JWT

    public User() {}

    public User(int id, String email, String username, String role, String token) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
