package com.lmello.spring_batch_demo.processors;

import com.lmello.spring_batch_demo.domain.BalanceRecord;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BalanceRecordProcessor implements ItemProcessor<BalanceRecord, BalanceRecord> {

    @Override
    public BalanceRecord process(BalanceRecord record) {
        require(
                !record.bank().isBlank() && record.bank().length() <= 20,
                record,
                "Bank must contain between 1 and 20 characters"
        );

        require(
                !record.agency().isBlank() && record.agency().length() <= 20,
                record,
                "Agency must contain between 1 and 20 characters"
        );

        require(
                record.currency().matches("[A-Z]{3}"),
                record,
                "Currency must contain three uppercase letters"
        );

        BigDecimal amount;
        try {
            amount = record.amount().setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Line " + record.lineNumber()
                            + ": amount requires rounding to two decimal places",
                    e
            );
        }

        require(
                amount.precision() <= 19,
                record,
                "Amount exceeds NUMERIC(19, 2)"
        );

        return new BalanceRecord(
                record.bank(),
                record.agency(),
                record.currency(),
                record.balanceDate(),
                amount,
                record.lineNumber(),
                record.rawLine()
        );
    }

    private void require(
            boolean condition,
            BalanceRecord record,
            String message
    ) {
        if (!condition) {
            throw new IllegalArgumentException("Line " + record.lineNumber() + ": " + message);
        }
    }
}
