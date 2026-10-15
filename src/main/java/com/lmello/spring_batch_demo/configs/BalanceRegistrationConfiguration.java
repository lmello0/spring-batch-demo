package com.lmello.spring_batch_demo.configs;

import com.lmello.spring_batch_demo.service.BalanceFileRegistrationService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;

@Configuration
public class BalanceRegistrationConfiguration {

    @Bean
    @StepScope
    public Tasklet registerFileTasklet(
            BalanceFileRegistrationService registrationService,
            @Value("#{jobParameters['filePath']}") String filePath
    ) {
        return (contribution, _) -> {
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("The filePath job parameter is required");
            }

            long fileId = registrationService.register(Path.of(filePath));

            contribution.getStepExecution()
                    .getExecutionContext()
                    .putLong("fileId", fileId);

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public ExecutionContextPromotionListener fileIdPromotionListener() {
        var listener = new ExecutionContextPromotionListener();

        listener.setKeys(new String[]{"fileId"});
        listener.setStrict(true);

        return listener;
    }

    @Bean
    public Step registerFileStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("registerFileTasklet") Tasklet tasklet,
            ExecutionContextPromotionListener fileIdPromotionListener
    ) {
        return new StepBuilder("registerFileStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(fileIdPromotionListener)
                .build();
    }
}
