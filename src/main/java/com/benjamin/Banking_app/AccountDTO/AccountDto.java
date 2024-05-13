package com.benjamin.Banking_app.AccountDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountDto {  //AccountDto class represents data transfer objects used for communication between
    // different layers, such as between the frontend and backend.
    private long id;
    private String accountUsername;
    private double balance;
} //Entities may contain sensitive information or business logic that should remain hidden from external consumers.
// DTOs act as a barrier that helps us expose only safe and relevant data to the clients.
