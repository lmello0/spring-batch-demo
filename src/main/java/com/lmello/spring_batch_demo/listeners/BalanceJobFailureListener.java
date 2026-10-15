package com.lmello.spring_batch_demo.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceJobFailureListener implements JobExecutionListener {

    private final JdbcClient jdbcClient;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        var context = jobExecution.getExecutionContext();

        if (!context.containsKey("fileId")) {
            return;
        }

        long fileId = context.getLong("fileId");

        jdbcClient.sql("""
                        UPDATE balance_file
                        SET status = 'FAILED'
                        WHERE id = :fileId
                        AND status <> 'COMPLETED'
                        """)
                .param("fileId", fileId)
                .update();
    }
}
