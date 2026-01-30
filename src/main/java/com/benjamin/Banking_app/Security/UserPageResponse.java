package com.benjamin.Banking_app.Security;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class UserPageResponse {
    private List<UserDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
