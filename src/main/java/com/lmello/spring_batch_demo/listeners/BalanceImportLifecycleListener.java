package com.lmello.spring_batch_demo.listeners;

import com.lmello.spring_batch_demo.service.BalanceFileIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
public class BalanceImportLifecycleListener implements StepExecutionListener {

    private final JdbcClient jdbcClient;
    private final BalanceFileIntegrityService integrityService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        var jobExecution = stepExecution.getJobExecution();
        var context = jobExecution.getExecutionContext();

        if (!context.containsKey("fileId")) {
            throw new IllegalStateException("fileId is missing from the job execution context");
        }

        long fileId = context.getLong("fileId");
        String filePath = jobExecution.getJobParameters().getString("filePath");

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("The filePath job parameter is required");
        }

        try {
            integrityService.verify(fileId, Path.of(filePath));
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not verify input file: " + filePath, exception);
        }

        int updated = jdbcClient.sql("""
                        UPDATE balance_file
                        SET status = 'PROCESSING'
                        WHERE id = :fileId
                        AND status IN ('PROCESSING', 'FAILED')
                        """)
                .param("fileId", fileId)
                .update();

        if (updated != 1) {
            throw new IllegalStateException("File " + fileId + " is missing or cannot be imported");
        }
    }
}
