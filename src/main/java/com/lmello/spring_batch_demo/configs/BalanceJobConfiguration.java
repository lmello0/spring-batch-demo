package com.lmello.spring_batch_demo.configs;

import com.lmello.spring_batch_demo.listeners.BalanceJobFailureListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BalanceJobConfiguration {

    @Bean
    @StepScope
    public Tasklet completeFileTasklet(
            JdbcClient jdbcClient,
            @Value("#{jobExecutionContext['fileId']}") Long fileId
    ) {
        return (contribution, _) -> {
            if (fileId == null) {
                throw new IllegalStateException("fileId is missing from the job execution context");
            }

            int updated = jdbcClient.sql("""
                            UPDATE balance_file
                            SET status = 'COMPLETED',
                                processed_at = CURRENT_TIMESTAMP
                            WHERE id = :fileId
                            """)
                    .param("fileId", fileId)
                    .update();

            if (updated != 1) {
                throw new IllegalStateException("Expected to updated one file, but updated " + updated);
            }

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step completeFileStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("completeFileTasklet") Tasklet tasklet
    ) {
        return new StepBuilder("completeFileStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job balanceImportJob(
            JobRepository jobRepository,
            @Qualifier("registerFileStep") Step registerFileStep,
            @Qualifier("importBalancesStep") Step importBalanceStep,
            @Qualifier("completeFileStep") Step completeFileStep,
            BalanceJobFailureListener failureListener
    ) {
        return new JobBuilder("balanceImportJob", jobRepository)
                .listener(failureListener)
                .start(registerFileStep)
                .next(importBalanceStep)
                .next(completeFileStep)
                .build();
    }
}
