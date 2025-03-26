package com.benjamin.Banking_app.Accounts;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/all")
    public ResponseEntity<AccountResponse> getAllAccounts(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageNo, pageSize));
    }

    @PostMapping("/dd")
    public ResponseEntity<String> directDebit(@RequestBody TransferRequest request){
        accountService.directDebit(request);
        return ResponseEntity.ok("Transfer successful");
    }

    @PostMapping("/create")
    public ResponseEntity<AccountDto> createAccount(
            @RequestBody @Valid AccountDto accountDto) {
        return new ResponseEntity<>(accountService.createAccount(accountDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
            AccountDto accountDto = accountService.getAccountById(id);
            return ResponseEntity.ok(accountDto);
    }

    @PutMapping("/{id}/deposit")
    public ResponseEntity<AccountDto> deposit(@PathVariable Long id,
                                              @RequestBody Map<String, Double> request) {
        Double amount = request.get("amount");
        AccountDto accountDto = accountService.deposit(id, amount);
        return ResponseEntity.ok(accountDto);
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AccountDto> withdraw(@PathVariable Long id,
                                               @RequestBody Map<String, Double> request) {
        double amount = request.get("amount");
        AccountDto accountDto = accountService.withdraw(id, amount);
        return ResponseEntity.ok(accountDto);
    }

    @PutMapping("user/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest transferRequest) {
        accountService.transfer(transferRequest);
        return ResponseEntity.ok("Transfer successful");
    }

    @DeleteMapping("/delete/{id}") //admin/
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok("account successfuly deleted");
    }
}