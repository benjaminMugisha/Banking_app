package com.benjamin.Banking_app.Security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IbanGeneratorTest {
    @Test
    void testIbanUniqueness() {
        // Generate 1000 IBANs and make sure they are all unique
        Set<String> ibans = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String iban = IbanGenerator.generateIban();
            assertFalse(ibans.contains(iban), "Duplicate IBAN generated");
            ibans.add(iban);
        }
    }

    @Test
    void testIbanPrefix() {
        String iban = IbanGenerator.generateIban();
        assertTrue(iban.startsWith("IE29BENJ"), "IBAN should start with IE29BENJ");
    }
}
