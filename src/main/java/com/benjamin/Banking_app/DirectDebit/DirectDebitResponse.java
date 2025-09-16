package com.benjamin.Banking_app.DirectDebit;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DirectDebitResponse {
    private List<DirectDebitDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
