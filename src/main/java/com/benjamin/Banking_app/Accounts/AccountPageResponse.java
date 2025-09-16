package com.benjamin.Banking_app.Accounts;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccountPageResponse {
    private List<AccountDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
