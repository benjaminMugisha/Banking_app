package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Roles.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {


    @NotBlank(message = "first name cannot be empty")
    private String firstName;
    @NotBlank(message = "last name cannot be empty")
    private String lastName;
    @NotBlank(message = "account username cant be empty")
    @Size(min=8, max = 15, message = "username must be between 8-15 characters long")
    private String accountUsername;
    private double balance;
    @Email(message = "invalid email format")
    private String email;
    @NotBlank(message = "password is required")
    @Size(min=8, max = 15, message = "Password must be between 8-15 characters long")
    private String password;
    private Role role;
}
