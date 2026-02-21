package com.benjamin.Banking_app.DirectDebit;

import java.math.BigDecimal;

interface DirectDebitService {
    DirectDebitResponse createDirectDebit(String toAccountUsername, BigDecimal amount);
    DirectDebitPageResponse getDirectDebits(int pageNo, int pageSize, String accountUsername);
    DirectDebitResponse cancelDirectDebit(Long id);
    DirectDebitDto getById(long id);
    DirectDebitResponse updateDirectDebit(Long directDebitId, BigDecimal amount);
    DirectDebitPageResponse getAll(int pageNo, int pageSize);
    DirectDebitPageResponse getActiveDds(int pageNo, int pageSize);
}
