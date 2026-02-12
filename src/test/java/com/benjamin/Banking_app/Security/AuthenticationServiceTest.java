package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Exception.BadRequestException;
import com.benjamin.Banking_app.Exception.EmailAlreadyExists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authManager;
    @Mock private JWTService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John", "Doe",
                "johnAccount",  BigDecimal.valueOf(1000),
                "john@mail.com", "Password1234");
    }

    @Test
    void register_shouldSaveUserAndReturnTokens() {
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1234")).thenReturn("hashedPw");
        when(jwtService.generateToken(any(Users.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(Users.class))).thenReturn("refreshToken");

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getAccountUsername()).isEqualTo("johnAccount");

        verify(userRepository).save(any(Users.class));
    }

    @Test
    void register_shouldFail_whenEmailExists() {
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExists.class)
                .hasMessageContaining("Email already in use.");
    }

    @Test
    void authenticate_shouldReturnTokens_whenCredentialsValid() {
        Users user = Users.builder().id(1L).email("john@mail.com").password("pw").role(Role.USER).build();
        user.setAccount(Account.builder().accountUsername("johnAccount")
                .balance(BigDecimal.valueOf(500)).user(user).build());

        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        AuthenticationRequest request = new AuthenticationRequest("john@mail.com", "1234");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("accessToken");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
