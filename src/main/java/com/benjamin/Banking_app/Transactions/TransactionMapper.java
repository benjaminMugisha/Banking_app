package com.benjamin.Banking_app.Transactions;

public class TransactionMapper {

    public static TransactionDto mapToTransactionDto(Transaction transaction) {

        if(transaction == null) return null;
        String toEmail = null;
        if(transaction.getToAccount() != null &&
        transaction.getToAccount().getUser() != null) {
            toEmail = transaction.getToAccount().getUser().getEmail();
        }

        return TransactionDto.builder()
                        .transactionId(transaction.getTransactionId())
                .type(transaction.getType())
                .balance(transaction.getBalance())
                .email(transaction.getAccount().getUser().getEmail())
                .amount(transaction.getAmount()).timeStamp(transaction.getTime()).toEmail(toEmail)
                .build();
    }
}
