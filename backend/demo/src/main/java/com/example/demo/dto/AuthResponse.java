package com.example.demo.dto;

public class AuthResponse {
    private String token;
    private String message;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    
    // Constructors
    public AuthResponse() {
    }
    
    public AuthResponse(String token, String message, Long userId, String email, 
                       String firstName, String lastName, String role) {
        this.token = token;
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Builder class
    public static class AuthResponseBuilder {
        private String token;
        private String message;
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        
        public AuthResponseBuilder token(String token) {
            this.token = token;
            return this;
        }
        
        public AuthResponseBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public AuthResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public AuthResponseBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public AuthResponseBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public AuthResponseBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public AuthResponseBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public AuthResponse build() {
            return new AuthResponse(token, message, userId, email, firstName, lastName, role);
        }
    }
}