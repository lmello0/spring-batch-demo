package com.lmello.spring_batch_demo.configs;

import com.lmello.spring_batch_demo.domain.BalanceRecord;
import com.lmello.spring_batch_demo.linemappers.BalanceLineMapper;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.nio.charset.StandardCharsets;

@Configuration
public class BalanceReaderConfiguration {

    @Bean
    @StepScope
    public FlatFileItemReader<BalanceRecord> balanceFileReader(
            @Value("#{jobParameters['filePath']}") String filePath
    ) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("The filePath job parameter is required");
        }

        return new FlatFileItemReaderBuilder<BalanceRecord>()
                .name("balanceFileReader")
                .resource(new FileSystemResource(filePath))
                .encoding(StandardCharsets.UTF_8.name())
                .strict(true)
                .linesToSkip(1)
                .skippedLinesCallback(header -> {
                    if (!header.equals("BANK;AGENCY;CURRENCY;DATE;AMOUNT;"))
                        throw new IllegalArgumentException("Unexpected balance file header: " + header);
                })
                .lineMapper(new BalanceLineMapper())
                .saveState(true)
                .build();
    }
}
