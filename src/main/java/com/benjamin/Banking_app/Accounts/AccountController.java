package com.benjamin.Banking_app.Accounts;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("api/v2/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountPageResponse> getAllAccounts(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
            AccountDto accountDto = accountService.getAccountById(id);
            return ResponseEntity.ok(accountDto);
    }

    @PatchMapping("deposit")
    public ResponseEntity<AccountResponse> deposit(
            @RequestBody Map<String, Double> request) {
        BigDecimal amount = BigDecimal.valueOf(request.get("amount"));
        AccountDto accountDto = accountService.deposit(amount);
        return ResponseEntity.ok(AccountResponse.builder()
                        .message("Deposit of €" +  amount + " successful. your new balance is: €"
                                + accountDto.getBalance())
                .build());
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @RequestBody Map<String, Double> request) {
        BigDecimal amount = BigDecimal.valueOf(request.get("amount"));
        AccountDto accountDto = accountService.withdraw(amount);
        return ResponseEntity.ok(AccountResponse.builder()
                .message("Withdraw of €" +  amount + " successful. your new balance is: €"
                        + accountDto.getBalance())
                .build());
    }

    @PatchMapping("/transfer")
    public ResponseEntity<AccountResponse> transfer(
            @Valid @RequestBody TransferRequest transferRequest) {
        AccountDto accountDto = accountService.transfer(transferRequest);
        return ResponseEntity.ok(AccountResponse.builder()
                .message("Transfer of €" +  transferRequest.getAmount() + " successful. " +
                        "your remaining balance is €" + accountDto.getBalance())
                .build());
    }


}
