package com.benjamin.Banking_app.DirectDebit;

public class DirectDebitMapper {
    public static DirectDebitDto mapToDirectDebitDto(DirectDebit directDebit) {
        if (directDebit == null) {
            return null;
        }

        return DirectDebitDto.builder()
                .id(directDebit.getId())
                .fromAccountUsername(directDebit.getFromAccount().getUser().getEmail())
                .toAccountUsername(directDebit.getToAccount().getUser().getEmail())
                .amount(directDebit.getAmount())
                .nextPaymentDate(directDebit.getNextPaymentDate())
                .active(directDebit.isActive())
                .build();
    }
}
