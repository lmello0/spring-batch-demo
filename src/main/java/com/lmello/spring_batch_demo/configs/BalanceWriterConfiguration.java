package com.lmello.spring_batch_demo.configs;

import com.lmello.spring_batch_demo.domain.BalanceRecord;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;

@Configuration
public class BalanceWriterConfiguration {

    @Bean
    @StepScope
    public JdbcBatchItemWriter<BalanceRecord> balanceFileWriter(
            DataSource dataSource,
            @Value("#{jobExecutionContext['fileId']}") Long fileId
    ) {
        if (fileId == null) {
            throw new IllegalStateException("fileId is missing from the job execution context");
        }

        return new JdbcBatchItemWriterBuilder<BalanceRecord>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO balance_record (
                            file_id,
                            line_number,
                            bank,
                            agency,
                            currency,
                            balance_date,
                            amount,
                            raw_line
                        ) VALUES (
                            :fileId,
                            :lineNumber,
                            :bank,
                            :agency,
                            :currency,
                            :balanceDate,
                            :amount,
                            :rawLine
                        )
                        """)
                .itemSqlParameterSourceProvider(record ->
                        new MapSqlParameterSource()
                                .addValue("fileId", fileId)
                                .addValue("lineNumber", record.lineNumber())
                                .addValue("bank", record.bank())
                                .addValue("agency", record.agency())
                                .addValue("currency", record.currency())
                                .addValue("balanceDate", record.balanceDate())
                                .addValue("amount", record.amount())
                                .addValue("rawLine", record.rawLine())
                )
                .assertUpdates(true)
                .build();
    }

}
