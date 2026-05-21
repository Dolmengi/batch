package com.dolmengi.batch.commons.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

@Slf4j
public class StepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("{} parameter is ({}).", stepExecution.getStepName(), stepExecution.getJobParameters());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("{} finish. summary: {}", stepExecution.getStepName(), stepExecution.getSummary());

        return stepExecution.getExitStatus();
    }

}
