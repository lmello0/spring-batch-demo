package com.lmello.spring_batch_demo.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BalanceRecord(
        String bank,
        String agency,
        String currency,
        LocalDate balanceDate,
        BigDecimal amount,
        int lineNumber,
        String rawLine
) {
}
