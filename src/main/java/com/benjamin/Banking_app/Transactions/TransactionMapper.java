package com.benjamin.Banking_app.Transactions;

public class TransactionMapper {
    public static Transaction MapToTransaction(TransactionDto transactionDto){
        if(transactionDto == null){
            return null;
        }

        return new Transaction(transactionDto.getTransactionId(),transactionDto.getAccount(),
                transactionDto.getType(), transactionDto.getAmount(),
                transactionDto.getTimestamp(), transactionDto.getDescription(),
                transactionDto.getToAccount()
                 );
    }
    public static TransactionDto MapToTransactionDto(Transaction transaction){
        if(transaction == null){
            return null;
        }

        return new TransactionDto(transaction.getTransactionId(),transaction.getAccount(),
                transaction.getType(), transaction.getAmount(),
                transaction.getTime(), transaction.getDescription(),
                transaction.getToAccount()
        );
    }
}
