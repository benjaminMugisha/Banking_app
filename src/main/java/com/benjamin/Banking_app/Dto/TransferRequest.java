package com.benjamin.Banking_app.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {
    private Long fromAccountId;
    private Long toAccountId;
    private Long amount;
}
