package com.benjamin.Banking_app.Security;

import java.security.SecureRandom;

public class IbanGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String COUNTRY_CODE = "IE";
    private static final String CHECK_DIGITS = "29";
    private static final String BANK_CODE = "BENJ";   // like AIBK in Ireland

    public static String generateIban() {
        long number = Math.abs(RANDOM.nextLong()) % 100_000_000_000_000L;
        String accountNumber = String.format("%014d", number);
        return COUNTRY_CODE + CHECK_DIGITS + BANK_CODE + accountNumber;
    }
}
