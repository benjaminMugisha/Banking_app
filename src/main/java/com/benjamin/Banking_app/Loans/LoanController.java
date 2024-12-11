package com.benjamin.Banking_app.Loans;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/loan")
public class LoanController {
    private final LoanServiceImpl service;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllLoans(){
        List<Loan> loans = service.getAllLoans();
        return ResponseEntity.ok(loans);
    }
    @PostMapping("apply")
    public ResponseEntity<LoanResponse> applyForLoan(@RequestBody LoanRequest loanRequest) {
        LoanResponse response = service.applyForLoan(loanRequest);
        //loan denied:
        if (response.getLoan() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("{id}/repay")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanResponse> repayLoanEarly(@PathVariable Long loanId,
                                       @RequestBody Map<String, Double> request) {
       double amount = request.get("amount");
       return ResponseEntity.ok(service.repayLoanEarly(loanId, amount));
    }

    //to repay(monthly repayments not fully) all loans at the same time
    @PutMapping("/admin/repay/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> process(){
        service.processMonthlyRepayments();
        return ResponseEntity.ok("all accounts monthly repayment was succesfuly done");
    }

    @DeleteMapping("/admin/delete/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteLoan(@PathVariable long accountId) {
        service.deleteLoan(accountId);
        return ResponseEntity.ok("loan successfully deleted");
    }

    //get a particular loan
    @GetMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Loan> getLoanByLoanId(@PathVariable Long loanId){
       return new ResponseEntity<>( service.getLoanByLoanId(loanId), HttpStatus.FOUND);
    }

    //get all loans of an account
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Loan>> getLoansByAccountId(@PathVariable Long accountId) {
        return new ResponseEntity<>(service.getLoansByAccountId(accountId), HttpStatus.FOUND);
    }
}
