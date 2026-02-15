package com.benjamin.Banking_app.Transactions;

public class TransactionMapper {

    public static TransactionDto mapToTransactionDto(Transaction transaction) {

        return transaction == null ? null :
                TransactionDto.builder()
                        .transactionId(transaction.getTransactionId())
//                        .accountUsername(transaction.getAccount().getAccountUsername())
                        .type(transaction.getType())
                        .amount(transaction.getAmount())
                        .timeStamp(transaction.getTime())
                        .toEmail(transaction.getToAccount().getUser().getEmail() != null
                        ? transaction.getToAccount().getUser().getEmail() : null)
                        .build();
    }
}
