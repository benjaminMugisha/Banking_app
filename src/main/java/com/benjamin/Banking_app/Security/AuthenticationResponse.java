package com.benjamin.Banking_app.Security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String refreshToken;
    private String token;
    private String accountUsername;
    private String iban;
}
