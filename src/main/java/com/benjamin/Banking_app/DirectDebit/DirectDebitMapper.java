package com.benjamin.Banking_app.DirectDebit;

public class DirectDebitMapper {
    public static DirectDebitDto mapToDirectDebitDto(DirectDebit directDebit) {
        if (directDebit == null) {
            return null;
        }

        return DirectDebitDto.builder()
                .id(directDebit.getId())
                .fromAccountUsername(directDebit.getFromAccount().getAccountUsername())
                .toAccountUsername(directDebit.getToAccount().getAccountUsername())
                .amount(directDebit.getAmount())
                .nextPaymentDate(directDebit.getNextPaymentDate())
                .active(directDebit.isActive())
                .build();
    }
}
