package com.benjamin.Banking_app.Transactions;

public class TransactionMapper {

    public static TransactionDto mapToTransactionDto(Transaction transaction) {

        return transaction == null ? null :
                TransactionDto.builder()
                        .transactionId(transaction.getTransactionId())
                        .accountUsername(transaction.getAccount().getAccountUsername())
                        .type(transaction.getType())
                        .amount(transaction.getAmount())
                        .timestamp(transaction.getTime())
                        .toAccountUsername(transaction.getToAccount() != null
                        ? transaction.getToAccount().getAccountUsername() : null)
                        .build();
    }
}
