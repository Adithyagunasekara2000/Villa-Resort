package com.example.demo.filter;

import com.example.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        String role = null;

        // Check if Authorization header exists and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            
            try {
                System.out.println("JWT Token received: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                username = jwtUtil.extractUsername(jwt);
                role = jwtUtil.extractRole(jwt);
                
                System.out.println("Extracted username: " + username);
                System.out.println("Extracted role: " + role);
                
            } catch (Exception e) {
                System.out.println("JWT validation error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Authorization header missing or not starting with Bearer for: " + request.getRequestURI());
        }

        // If username is extracted and no authentication exists in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Create a UserDetails object
            org.springframework.security.core.userdetails.User userDetails = 
                new org.springframework.security.core.userdetails.User(
                    username, 
                    "", 
                    Collections.singletonList(new SimpleGrantedAuthority(role != null ? role : "ROLE_USER"))
                );
            
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
                );
            
            authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("Authentication set for user: " + username);
        } else {
            System.out.println("Username is null or already authenticated");
        }
        
        filterChain.doFilter(request, response);
    }
}