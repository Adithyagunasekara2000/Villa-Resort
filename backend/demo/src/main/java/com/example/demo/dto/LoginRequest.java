package com.example.demo.dto;

public class LoginRequest {
    
    private String email;
    private String password;
    
    // Default constructor (REQUIRED)
    public LoginRequest() {
    }
    
    // Parameterized constructor (optional)
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    // Getters
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    // Setters (REQUIRED for Spring to populate the object)
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}