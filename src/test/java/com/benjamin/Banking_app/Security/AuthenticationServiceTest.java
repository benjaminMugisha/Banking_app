package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.BadRequestException;
import com.benjamin.Banking_app.Roles.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_NewUser_ReturnsAuthenticationResponse() {
        RegisterRequest request = RegisterRequest.builder()
                .email("john@gmail.com").password("password").accountUsername("john12345")
                .balance(1000.0).role(Role.USER)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(Users.class))).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getAccount());
        assertEquals("john12345", response.getAccount().getAccountUsername());

        verify(userRepository).save(any(Users.class));
    }

    @Test
    void register_ExistingEmail_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("john@gmail.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authenticationService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_ValidCredentials_ReturnsJwtToken() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john@gmail.com").password("password")
                .build();

        Users user = Users.builder()
                .email(request.getEmail()).password("encodedPassword").role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(authManager).authenticate(any());
    }

    @Test
    void authenticate_InvalidEmail_ThrowsAccessDeniedException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("invalid@gmail.com").password("password")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void authenticate_AuthManagerFails_ThrowsAccessDeniedException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("wrong@gmail.com").password("password")
                .build();

        Users mockUser = Users.builder()
                .email("john@gmail.com").password("password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(mockUser));

        doThrow(new RuntimeException("bad credentials")).when(authManager).authenticate(any());

        assertThrows(AccessDeniedException.class, () -> authenticationService.authenticate(request));
    }
}
