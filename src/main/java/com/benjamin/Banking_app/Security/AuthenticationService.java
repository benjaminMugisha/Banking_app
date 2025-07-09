package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
        if(userRepository.existsByEmail(request.getEmail())){
            logger.warn("attempt to register with duplicate email: {}", request.getEmail());
            throw new BadRequestException("email already in use.");
        }
        var user = Users.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())  
                .build();

        //creating an account linked to the user:
        var account = Account.builder()
                .accountUsername(request.getAccountUsername())
                .balance(request.getBalance())
                .user(user)
                .build();

        user.setAccount(account); //connecting account to user

        try {
            userRepository.save(user); // saves both user and account due to cascade
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
