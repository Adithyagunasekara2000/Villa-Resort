package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        if (secret.length() < 32) {
            StringBuilder paddedSecret = new StringBuilder(secret);
            while (paddedSecret.length() < 32) {
                paddedSecret.append("0");
            }
            secret = paddedSecret.toString();
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .subject(username)  // This sets the username as the subject
                .claims(claims) 
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())  // Remove Jwts.SIG.HS256, just use signWith(key)
                .compact();
    }
    
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.out.println("Error extracting username: " + e.getMessage());
            return null;
        }
    }
    
    public String extractRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String role = claims.get("role", String.class);
            if (role != null && !role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
            return role;
        } catch (Exception e) {
            System.out.println("Error extracting role: " + e.getMessage());
            return "ROLE_USER";
        }
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  
                .build()
                .parseSignedClaims(token)  
                .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return false;
        }
    }
    
    public Boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}