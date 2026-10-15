package com.lmello.spring_batch_demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@Component
public class BalanceFolderRunner implements ApplicationRunner {

    private final JobOperator jobOperator;
    private final Job job;
    private final Path inputFolder;

    public BalanceFolderRunner(
            JobOperator jobOperator,
            @Qualifier("balanceImportJob") Job job,
            @Value("${app.input-folder}") String inputFolder
    ) {
        this.jobOperator = jobOperator;
        this.job = job;
        this.inputFolder = Path.of(inputFolder);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!Files.isDirectory(inputFolder)) {
            throw new IllegalArgumentException("Input folder does not exist: " + inputFolder);
        }

        try (var paths = Files.list(inputFolder)) {
            var files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".csv"))
                    .sorted()
                    .toList();

            for (Path file : files) {
                importFile(file);
            }
        }
    }

    private void importFile(Path file) {
        var parameters = new JobParametersBuilder()
                .addString("filePath", file.toString())
                .toJobParameters();

        try {
            var execution = jobOperator.start(job, parameters);

            log.info("Import for {} returned status {}", file, execution.getStatus());
        } catch (JobInstanceAlreadyCompleteException exception) {
            log.info("Skipping already completed file: {}", file);
        } catch (Exception exception) {
            log.error("Could not launch import for {}", file, exception);
        }
    }
}
