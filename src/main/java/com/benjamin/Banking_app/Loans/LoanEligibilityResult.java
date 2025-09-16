package com.benjamin.Banking_app.Loans;

import java.math.BigDecimal;

public record LoanEligibilityResult(boolean isAffordable, BigDecimal dti) {}