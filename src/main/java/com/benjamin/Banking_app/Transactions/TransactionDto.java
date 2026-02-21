package com.benjamin.Banking_app.Transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {
    private Long transactionId;
    private String email;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime timeStamp;
    private String toEmail;
    private BigDecimal balance;
}
