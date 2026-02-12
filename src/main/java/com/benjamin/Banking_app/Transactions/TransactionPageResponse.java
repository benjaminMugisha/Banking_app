package com.benjamin.Banking_app.Transactions;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class TransactionPageResponse {
    private List<TransactionDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
