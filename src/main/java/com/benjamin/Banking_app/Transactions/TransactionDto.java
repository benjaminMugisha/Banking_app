package com.benjamin.Banking_app.Transactions;

import com.benjamin.Banking_app.Accounts.Account;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long transactionId;
    @JsonIgnore
    private Account account;
    private String type;
    private Double amount;
    @JsonFormat(pattern = "dd/MM/yyy HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;
    private String description;


    @JsonIgnore
    private Account toAccount;

//    private String toAccountUsername;


}
