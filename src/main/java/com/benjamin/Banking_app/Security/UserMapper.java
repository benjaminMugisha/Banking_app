package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;

public class UserMapper {
    public static UserDto MapToUserDto(Users user) {
        if (user == null) {
            return null;
        }
        Account account = user.getAccount();
//        return new UserDto(user.getId(), user.getAccount().getAccountUsername(),
//                        user.getAccount().getBalance(),
//                        user.getFirstName(), user.getLastName(),
//                        user.getEmail(), user.getAccount().getIban(), user.getRole());
        return UserDto.builder()
                .id(user.getId()).firstname(user.getFirstName())
                .lastname(user.getLastName()).email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .accountUsername(account != null ? account.getAccountUsername() : null)
                .accountBalance(account != null ? account.getBalance() : null)
                .iban(account != null ? account.getIban() : null)
                .build();
    }
}
