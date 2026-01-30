package com.benjamin.Banking_app.Loans;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/loans")
public class LoanController {
    private final LoanServiceImpl service;

    @GetMapping("/account")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public LoanPageResponse getLoansByAccountId(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String accountUsername
    ) {
        return service.getLoansOfAnAccount(pageNo, pageSize, accountUsername);
    }

    @PostMapping("apply")
    public ResponseEntity<LoanResponse> applyForLoan(@RequestBody @Valid LoanRequest loanRequest) {
        LoanResponse response = service.applyForLoan(loanRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/repay-full/{loanId}")
    public ResponseEntity<LoanResponse> repayFullLoanEarly(@PathVariable Long loanId) {
        return ok(service.repayLoanEarly(loanId));
    }

    @PatchMapping("/repay/{loanId}")
    public ResponseEntity<LoanResponse> customLoan(
            @PathVariable Long loanId,
            @RequestBody Map<String, Double> request) {
        BigDecimal amount = BigDecimal.valueOf(request.get("amount"));
        return ok(service.repayCustomAmount(loanId, amount));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDto> getLoanByLoanId(@PathVariable long loanId){
         return ResponseEntity.ok(service.getLoanByLoanId(loanId));
    }
}
