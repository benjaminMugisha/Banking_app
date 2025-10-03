package com.benjamin.Banking_app.DirectDebit;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/dd")
@RequiredArgsConstructor
public class DirectDebitController {

    private final DirectDebitService directDebitService;

    //fetch direct debits belonging to current user or admin searches direct debits of a specific account.
    @GetMapping("get")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DirectDebitResponse getActiveDirectDebits(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String accountUsername) {
        return directDebitService.getDirectDebits(pageNo, pageSize, accountUsername);
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DirectDebitDto> getDirectDebit(@PathVariable long id) {
        DirectDebitDto dto = directDebitService.getById(id);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create")
    public ResponseEntity<DirectDebitDto> createDirectDebit(
            @Valid @RequestBody DirectDebitRequest request){
        return new ResponseEntity<>(
                directDebitService.createDirectDebit(request.getToIban(), request.getAmount()),
                HttpStatus.CREATED);
    }

    @PatchMapping("/cancel/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> cancelDirectDebit(@PathVariable Long id) {
        directDebitService.cancelDirectDebit(id);
        return ResponseEntity.ok(
                "Direct debit with id: " + id + " has been cancelled");
    }
}
