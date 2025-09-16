package com.benjamin.Banking_app.DirectDebit;

import java.math.BigDecimal;
import java.util.List;

interface DirectDebitService {
    DirectDebitDto createDirectDebit(String toAccountUsername, BigDecimal amount);
    DirectDebitResponse getDirectDebits(int pageNo, int pageSize, String accountUsername);
    void cancelDirectDebit(Long id);
    DirectDebitDto getById(long id);
    List<DirectDebit> all();
}
