package com.benjamin.Banking_app.Security;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String accountUsername;
    private BigDecimal accountBalance;
    private String firstname;
    private String lastname;
    private String email;
    private String iban;
    private Role role;
    private boolean active;
}
