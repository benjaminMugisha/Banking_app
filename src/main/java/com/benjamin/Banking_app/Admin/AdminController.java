package com.benjamin.Banking_app.Admin;

import com.benjamin.Banking_app.DirectDebit.DirectDebitPageResponse;
import com.benjamin.Banking_app.DirectDebit.DirectDebitServiceImpl;
import com.benjamin.Banking_app.Loans.LoanPageResponse;
import com.benjamin.Banking_app.Loans.LoanServiceImpl;
import com.benjamin.Banking_app.Security.AuthenticationService;
import com.benjamin.Banking_app.Security.UserDto;
import com.benjamin.Banking_app.Security.UserPageResponse;
import com.benjamin.Banking_app.Transactions.TransactionPageResponse;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminServiceImpl adminService;
//    private final DirectDebitServiceImpl ddService;
//    private final LoanServiceImpl loanService;
//    private final TransactionServiceImpl txService;
//    private final AccountServiceImpl accountService;
//    private final AuthenticationService authService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }





}
