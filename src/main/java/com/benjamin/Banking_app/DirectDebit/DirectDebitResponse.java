package com.benjamin.Banking_app.DirectDebit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectDebitResponse {
    private DirectDebitDto dto;
    private DDStatusMessage status;

    public DirectDebitResponse(DirectDebitDto dto, DDStatusMessage status) {
        this.dto = dto;
        this.status = status;
    }
}
