package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.DirectDebit.DirectDebitResponse;
import com.benjamin.Banking_app.DirectDebit.DirectDebitServiceImpl;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InvalidJwtSignatureException;
import com.benjamin.Banking_app.Loans.LoanPageResponse;
import com.benjamin.Banking_app.Loans.LoanServiceImpl;
import com.benjamin.Banking_app.Transactions.TransactionResponse;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationService service;
    private final DirectDebitServiceImpl directDebitService;
    private final TransactionServiceImpl transactionService;
    private final LoanServiceImpl loanService;
    private final JWTService jwtService;
    private final UserRepository userRepository;


    @PostMapping("/register")
    public AuthenticationResponse register(
            @RequestBody @Valid RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser() {
        return ResponseEntity.ok(service.getUserInfo());
    }

    @GetMapping("/me/direct-debits")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DirectDebitResponse getActiveDirectDebits(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String accountUsername) {
        return directDebitService.getDirectDebits(pageNo, pageSize, accountUsername);
    }

    @GetMapping("/me/transactions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionHistory(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String accountUsername) {
        TransactionResponse transactions = transactionService.transactions(pageNo, pageSize, accountUsername);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/me/loans")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public LoanPageResponse getLoansByAccountId(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String accountUsername
    ) {
        return loanService.getLoansOfAnAccount(pageNo, pageSize, accountUsername);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");
        String userEmail = jwtService.extractUserName(refreshToken);

        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new InvalidJwtSignatureException("Refresh token is invalid");
        }

        String newAccessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .accountUsername(user.getAccount().getAccountUsername())
                .build());
    }
}
