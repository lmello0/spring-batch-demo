package com.lmello.spring_batch_demo.configs;

import com.lmello.spring_batch_demo.domain.BalanceRecord;
import com.lmello.spring_batch_demo.processors.BalanceRecordProcessor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BalanceImportConfiguration {

    @Bean
    public Step importBalancesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<BalanceRecord> balanceFileReader,
            BalanceRecordProcessor processor,
            JdbcBatchItemWriter<BalanceRecord> balanceFileWriter
    ) {
        return new StepBuilder("importBalanceStep", jobRepository)
                .<BalanceRecord, BalanceRecord>chunk(100)
                .transactionManager(transactionManager)
                .reader(balanceFileReader)
                .processor(processor)
                .writer(balanceFileWriter)
                .build();
    }
}
