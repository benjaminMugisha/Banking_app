package com.benjamin.Banking_app.Security;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "first name cannot be empty")
    @Size(max = 20, message = "First name cannot exceed 20 characters")
    private String firstName;

    @NotBlank(message = "last name cannot be empty")
    @Size(max = 20, message = "Last name cannot exceed 20 characters")
    private String lastName;

    @NotBlank(message = "account username cant be empty")
    @Size(min=8, max = 15, message = "username must be between 8-15 characters long")
    private String accountUsername;

    @Min(value=5, message = "initial balance can't be less than 5 Euros")
    @Max(value = 10000000, message = "you're not a millionaire")
    private BigDecimal balance;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "invalid email format")
    @Size(max = 50, message = "Email can't exceed 50 characters.")
    private String email;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter and one number"
    )
    @Size(min=7, max = 15)
    private String password;
    private Role role;
}
