package com.benjamin.Banking_app.DirectDebit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class DirectDebitDto {
    private Long id;
    private String fromAccountUsername;
    private String toAccountUsername;
    private BigDecimal amount;
    private LocalDate nextPaymentDate;
    private boolean active;
}
