package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.model.MembershipLevel;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public String test() {
        return "API WORKING!";
    }

   
  @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    try {
        // Manual validation
        Map<String, String> errors = new HashMap<>();
        
        // Validate firstName
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            errors.put("firstName", "First name is required");
        } else if (request.getFirstName().length() < 2 || request.getFirstName().length() > 50) {
            errors.put("firstName", "First name must be between 2 and 50 characters");
        }
        
        // Validate lastName
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            errors.put("lastName", "Last name is required");
        } else if (request.getLastName().length() < 2 || request.getLastName().length() > 50) {
            errors.put("lastName", "Last name must be between 2 and 50 characters");
        }
        
        // Validate email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!request.getEmail().contains("@") || !request.getEmail().contains(".")) {
            errors.put("email", "Please provide a valid email address");
        }
        
        // Validate password
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        } else if (request.getPassword().length() < 8) {
            errors.put("password", "Password must be at least 8 characters long");
        } else {
            // Simple password strength check
            String password = request.getPassword();
            boolean hasUpper = !password.equals(password.toLowerCase());
            boolean hasLower = !password.equals(password.toUpperCase());
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
            
            if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
                errors.put("password", "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character");
            }
        }
        
        // Validate phone
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            errors.put("phone", "Phone number is required");
        } else if (!request.getPhone().matches("^[+]?[0-9]{10,15}$")) {
            errors.put("phone", "Please provide a valid phone number (10-15 digits)");
        }
        
        // Validate role
        Role userRole = Role.ROLE_USER; // Default role
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                userRole = Role.valueOf(request.getRole().toUpperCase());
                // Ensure only valid roles are accepted
                if (!Arrays.asList(Role.values()).contains(userRole)) {
                    errors.put("role", "Invalid role. Must be ROLE_USER or ROLE_ADMIN");
                }
            } catch (IllegalArgumentException e) {
                errors.put("role", "Invalid role. Must be ROLE_USER or ROLE_ADMIN");
            }
        }
        
        // Check if there are any validation errors
        if (!errors.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Validation failed");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(error);
        }

        // Create new user
        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setPhone(request.getPhone());
        newUser.setRole(userRole);  // Use the role from request
        newUser.setEnabled(true);
        newUser.setAccountNonExpired(true);
        newUser.setAccountNonLocked(true);
        newUser.setCredentialsNonExpired(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setMembershipLevel(MembershipLevel.STANDARD);

        User savedUser = userRepository.save(newUser);

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "token", token,
                "userId", savedUser.getId(),
                "email", savedUser.getEmail(),
                "firstName", savedUser.getFirstName(),
                "lastName", savedUser.getLastName(),
                "role", savedUser.getRole().name()  // Return the actual role
        ));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(Map.of("message", "Registration failed: " + e.getMessage()));
    }
}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Manual validation for login
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email is required"));
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Password is required"));
            }
            
            // Simple email format check
            String email = loginRequest.getEmail();
            if (!email.contains("@") || !email.contains(".")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Please provide a valid email address"));
            }

            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Invalid email or password"));
            }

            User user = userOptional.get();

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Invalid email or password"));
            }

            if (!user.isEnabled()) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Account is disabled"));
            }

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token,
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole(),
                    "membershipLevel", user.getMembershipLevel()
            ));
            
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("valid", false, "message", "Token is required"));
            }
            
            // First validate the token structure and expiration
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.ok(Map.of("valid", false, "message", "Invalid or expired token"));
            }
            
            String email = jwtUtil.extractUsername(token);
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "User not found"
                ));
            }

            // Additional validation with username
            boolean isValid = jwtUtil.validateToken(token, email);

            if (!isValid) {
                return ResponseEntity.ok(Map.of("valid", false, "message", "Token validation failed"));
            }

            User user = userOptional.get();

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "role", user.getRole(),
                            "membershipLevel", user.getMembershipLevel()
                    )
            ));
            
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            return ResponseEntity.ok(Map.of("valid", false, "message", "Token validation error: " + e.getMessage()));
        }
    }
    
    // Add a simple health check endpoint
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "Auth Service");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/create-admin-force")
    public ResponseEntity<?> createAdminForce(@RequestBody RegisterRequest request) {
        try {
            // Skip role validation, force ADMIN
            User newUser = new User();
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setPhone(request.getPhone());
            
            // Force ADMIN role
            newUser.setRole(Role.ROLE_ADMIN);
            System.out.println("FORCING ROLE: ADMIN");
            
            newUser.setEnabled(true);
            newUser.setAccountNonExpired(true);
            newUser.setAccountNonLocked(true);
            newUser.setCredentialsNonExpired(true);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            newUser.setMembershipLevel(MembershipLevel.STANDARD);

            User savedUser = userRepository.save(newUser);
            
            System.out.println("SAVED USER ROLE: " + savedUser.getRole());

            return ResponseEntity.ok(Map.of(
                    "message", "Admin created successfully (forced)",
                    "role", savedUser.getRole().name(),
                    "email", savedUser.getEmail()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }
}