package com.benjamin.Banking_app.DirectDebit;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/dd")
@RequiredArgsConstructor
public class DirectDebitController {

    private final DirectDebitService directDebitService;

    //fetch direct debits belonging to current user or admin searches direct debits of a specific account.
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DirectDebitPageResponse getDirectDebits(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String email) {
        return directDebitService.getDirectDebits(pageNo, pageSize, email);
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DirectDebitDto> getDirectDebit(@PathVariable long id) {
        DirectDebitDto dto = directDebitService.getById(id);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DirectDebitResponse> createDirectDebit(
            @Valid @RequestBody DirectDebitRequest request){
        return new ResponseEntity<>(
                directDebitService.createDirectDebit(request.getToIban(), request.getAmount()),
                HttpStatus.CREATED);
    }

    @PatchMapping("/cancel/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DirectDebitResponse> cancelDirectDebit(@PathVariable Long id) {
        return new ResponseEntity<>(
                directDebitService.cancelDirectDebit(id), HttpStatus.OK);
    }

    @PatchMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DirectDebitResponse> updateDirectDebit(
            @PathVariable Long id, @RequestBody Map<String, Double> request) {
        BigDecimal amount = BigDecimal.valueOf(request.get("amount"));
        return new ResponseEntity<>(
                directDebitService.updateDirectDebit(id, amount), HttpStatus.ACCEPTED);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dd")
    public ResponseEntity<DirectDebitPageResponse> getAllDirectDebits(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return ResponseEntity.ok(directDebitService.getAll(pageNo, pageSize));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/activedd")
    public ResponseEntity<DirectDebitPageResponse> getActiveDirectDebits(
            @RequestParam(value = "pageNo",defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return ResponseEntity.ok(directDebitService.getActiveDds(pageNo, pageSize));
    }
}
