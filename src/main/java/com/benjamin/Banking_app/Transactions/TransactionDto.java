package com.benjamin.Banking_app.Transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String accountUsername;
    private TransactionType type;
    private BigDecimal amount;
    @JsonFormat(pattern = "dd/MM/yyy HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;
}
