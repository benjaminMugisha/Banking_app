package com.benjamin.Banking_app.Security;

public class UserMapper {
    public static UserDto MapToUserDto(Users user) {
        return user == null ? null :
                new UserDto(user.getId(), user.getAccount().getAccountUsername(),
                        user.getAccount().getBalance(),
                        user.getFirstName(), user.getLastName(),
                        user.getEmail(), user.getAccount().getIban(), user.getRole());
    }
}
