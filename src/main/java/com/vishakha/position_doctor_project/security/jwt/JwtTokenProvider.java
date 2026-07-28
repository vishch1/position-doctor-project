package com.vishakha.position_doctor_project.security.jwt;

import org.springframework.stereotype.Component;

/**
 * Placeholder JWT token provider component.
 */
@Component
public class JwtTokenProvider {

    public String generateToken(String username) {
        return "placeholder-jwt-token-for-" + username;
    }

    public boolean validateToken(String token) {
        return token != null && !token.isBlank();
    }

    public String getUsernameFromToken(String token) {
        return "placeholderUser";
    }
}
