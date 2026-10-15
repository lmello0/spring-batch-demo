package com.lmello.spring_batch_demo.linemappers;

import com.lmello.spring_batch_demo.domain.BalanceRecord;
import org.springframework.batch.infrastructure.item.file.LineMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BalanceLineMapper implements LineMapper<BalanceRecord> {

    @Override
    public BalanceRecord mapLine(String line, int lineNumber) throws Exception {
        String[] fields = line.split(";", -1);

        if (fields.length != 6 || !fields[5].isEmpty()) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber
                            + ": expected BANK;AGENCY;CURRENCY;DATE;AMOUNT;"
            );
        }

        return new BalanceRecord(
                fields[0].strip(),
                fields[1].strip(),
                fields[2].strip(),
                LocalDate.parse(fields[3].strip()),
                new BigDecimal(fields[4].strip()),
                lineNumber,
                line
        );
    }
}
