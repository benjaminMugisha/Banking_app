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
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanPageResponse> getAllLoans(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ){
        LoanPageResponse loans = service.getAllLoans(pageNo, pageSize);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Loan>> getLoansByAccountId(@PathVariable Long accountId) {
        return new ResponseEntity<>(service.getLoansByAccountId(accountId), HttpStatus.FOUND);
    }

    @PostMapping("apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanResponse> applyForLoan(@RequestBody LoanRequest loanRequest) {
        LoanResponse response = service.applyForLoan(loanRequest);
        //loan denied:
        if (response.getLoan() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("{loanId}/repay")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanResponse> monthlyRepay(@PathVariable Long loanId,
                                       @RequestBody(required = false) Map<String, Double> request) {
    double amount;
    //if request is null or empty, then we use the predefined default amount to repay each month
    if(request == null || request.isEmpty() || !request.containsKey("amount")){
        amount = 0;
    } else {
        amount = request.get("amount");
    }
       return ResponseEntity.ok(service.processMonthlyRepayment(loanId, amount));
    }

    @PutMapping("/{loanId}/repayFull")
    public ResponseEntity<LoanResponse> repayFullLoanEarly(@PathVariable Long loanId) {
        return ResponseEntity.ok(service.repayLoanEarly(loanId));
    }

    @DeleteMapping("/delete/{loanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteLoan(@PathVariable long loanId) {
        service.deleteLoan(loanId);
        return ResponseEntity.ok("loan successfully deleted");
    }
    @GetMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Loan> getLoanByLoanId(@PathVariable Long loanId){
       return new ResponseEntity<>( service.getLoanByLoanId(loanId), HttpStatus.FOUND);
    }


}