package com.vishakha.position_doctor_project.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "testSecretKeyForPositionDoctorPlatformUnitTesting1234567890";
        tokenProvider = new JwtTokenProvider(secret, 3600000);
    }

    @Test
    @DisplayName("generateToken - Creates valid JWT token and extracts username")
    void testGenerateAndValidateToken() {
        String email = "satoshi@positiondoctor.com";
        String token = tokenProvider.generateToken(email);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(email, tokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("validateToken - Rejects invalid token string")
    void testValidateToken_InvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
    }
}
