package com.benjamin.Banking_app.Accounts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AccountDto {
    private Long id;

    @NotBlank(message = "username cannot be empty")
    private String accountUsername;
    @Min(message = "balance cannot be less than one euro", value = 1)
    private double balance;
}
