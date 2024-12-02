package com.benjamin.Banking_app.Loans;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class LoanController {
    private final LoanServiceImpl service;

    @GetMapping("admin/loan/get")
    public ResponseEntity<List<Loan>> getAllLoans(){
        List<Loan> loans = service.getAllLoans();
        return ResponseEntity.ok(loans);
    }
    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> applyForLoan(@RequestBody LoanRequest loanRequest) {
        LoanResponse response = service.applyForLoan(loanRequest);
        //loan denied:
        if (response.getLoan() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/{id}/repay")
    public ResponseEntity<LoanResponse> repayLoanEarly(@PathVariable Long loanId,
                                       @RequestBody Map<String, Double> request) {
       double amount = request.get("amount");
       return ResponseEntity.ok(service.repayLoanEarly(loanId, amount));
    }

    //to repay(monthly not fully) all loans at the same time
    @PutMapping("/admin/repay/all")
    public ResponseEntity<String> process(){
        service.processMonthlyRepayments();
        return ResponseEntity.ok("all accounts monthly repayment was succesfuly done");
    }

    @DeleteMapping("/admin/delete/{accountId}")
    public ResponseEntity<String> deleteLoan(@PathVariable long accountId) {
        service.deleteLoan(accountId);
        return ResponseEntity.ok("loan succesfully deleted");
    }

    @GetMapping("/user/loan/{loanId}")
    public ResponseEntity<Loan> getLoanByLoanId(@PathVariable Long loanId){
       return new ResponseEntity<>( service.getLoanByLoanId(loanId), HttpStatus.FOUND);
    }

    @GetMapping("/user/account/{id}")
    public ResponseEntity<List<Loan>> getLoansByAccountId(@PathVariable Long id) {
        return new ResponseEntity<>(service.getLoansByAccountId(id), HttpStatus.FOUND);
    }
}
