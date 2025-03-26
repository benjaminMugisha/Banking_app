package com.benjamin.Banking_app.Accounts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransferRequest {
    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;
}
