package com.benjamin.Banking_app.Transactions;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionServiceImpl transactionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TransactionResponse getTransactions(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String email) {

        return transactionService.transactions(pageNo, pageSize, email);
    }

    @GetMapping("/tx")
    public ResponseEntity<TransactionPageResponse> getAllTransactions(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactions(pageNo,pageSize));
    }
}
