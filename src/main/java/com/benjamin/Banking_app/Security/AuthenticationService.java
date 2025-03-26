package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.BadCredentialsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    public AuthenticationResponse register(RegisterRequest request){
        //checking if the user's email already exists
        if(userRepository.existsByEmail(request.getEmail())){
            logger.warn("attempt to register with duplicate email: {}", request.getEmail());
            throw new BadCredentialsException ("email already in use");
        }
        var user = Users.builder()
                .firstname(request.getFirstName()) //getting user's details
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())  //assigning role
                .build();
        try {
            userRepository.save(user);
            logger.info("user: {} successfully registered", request.getEmail());
        } catch (Exception e) {
            logger.error("failed to register user: {}, error: {}", request.getEmail(), e.getMessage());
            throw new BadCredentialsException("registration failed.");
        }
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        try {
            authManager.
                    authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AccessDeniedException("user not found. try again"));

            var jwtToken = jwtService.generateToken(user);
            logger.info("User: {} logged in successfully.", request.getEmail());

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        } catch (Exception e) {
            logger.error("failed login attempt for user: {}. Error: {}", request.getEmail(), e.getMessage());
            throw new AccessDeniedException("Login failed");
        }
    }
}