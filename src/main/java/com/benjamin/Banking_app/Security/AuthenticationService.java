package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Exception.BadRequestException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationResponse register(RegisterRequest request){
        logger.info("user creation underway...");
        if(userRepository.existsByEmail(request.getEmail())){
            logger.warn("attempt to register with duplicate email: {}", request.getEmail());
            throw new BadRequestException("email already in use.");
        }
        var role = (request.getRole() == null ) ? Role.USER : request.getRole();
        
        var user = Users.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        var account = Account.builder()
                .accountUsername(request.getAccountUsername())
                .balance(request.getBalance())
                .iban(generateUniqueIban())
                .user(user)
                .build();

        user.setAccount(account);

        try {
            userRepository.save(user);
            logger.info("user: {} successfully registered", request.getEmail());
        } catch (Exception e) {
            logger.error("failed to register user: {}, error: {}", request.getEmail(), e.getMessage());
            throw new BadRequestException("registration failed.");
        }
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .accountUsername(account.getAccountUsername())
                .iban(user.getAccount().getIban())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        try {
            var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("user not found. try again"));

            authManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            logger.info("User {} logged in succesfully", request.getEmail());

            return AuthenticationResponse.builder()
                    .token(accessToken).refreshToken(refreshToken)
                    .accountUsername(user.getAccount().getAccountUsername())
                    .build();
        } catch (BadCredentialsException e){
            logger.warn("wrong password for email: {}", request.getEmail());
            throw new BadRequestException("Wrong credentials. Please try again");
        }
        catch (Exception e) {
            logger.error("failed login attempt for user: {}. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    public UserDto getUserInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return UserDto.builder()
                .accountUsername(user.getAccount().getAccountUsername())
                .accountBalance(user.getAccount().getBalance())
                .iban(user.getAccount().getIban())
                .email(user.getEmail())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .role(user.getRole())
                .build();
    }

    private String generateUniqueIban() {
        int attempts = 0;
        while (attempts < 5) { //5 attempts to create an iban.
            String iban = IbanGenerator.generateIban();
            if (!userRepository.existsByAccount_Iban(iban)) {
                return iban;
            }
            attempts++;
        }
        throw new IllegalStateException("Could not generate unique IBAN after 5 retries");
    }

    public UserPageResponse getUsers(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Users> users = userRepository.findAll(pageable);

        List<UserDto> content = users.stream()
                .map(UserMapper::MapToUserDto)
                .collect(Collectors.toList());
        int totalPages = users.getTotalPages() == 0 ? 1 : users.getTotalPages();

        return UserPageResponse.builder()
                .content(content).pageNo(users.getNumber()).pageSize(users.getSize())
                .totalElements(users.getTotalElements()).totalPages(totalPages)
                .last(users.isLast())
                .build();

    }
}
