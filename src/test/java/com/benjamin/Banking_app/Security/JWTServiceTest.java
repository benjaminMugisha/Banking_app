package com.benjamin.Banking_app.Security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
       "PhC93p4UQO4d1Yj9RLx6qOIwbbLAjUfebwuTjyeYfhc="
})
class JWTServiceTest {

    @Autowired
    private JWTService jwtService;

    private final UserDetails userDetails = new User("John", "password", List.of());

    @Test
    void testGenerateAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUserName(token);

        assertEquals("John", username);
    }

    @Test
    void testIsTokenValid_shouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_shouldReturnFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("Peter", "password", List.of());

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    void testTokenIsNotExpired() {
        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
}


