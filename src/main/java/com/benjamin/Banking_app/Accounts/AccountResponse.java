package com.benjamin.Banking_app.Accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AccountResponse<T> {
    private String message;
    private T data;
}
