package com.dolmengi.batch.commons.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

@Slf4j
public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("{} start.", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("{} finish. jobId: {}, jobParam: {}", jobExecution.getJobInstance().getJobName(), jobExecution.getJobInstanceId(), jobExecution.getJobParameters());
        } else {
            log.error("{} failed({}). jobId: {}, jobParam: {}", jobExecution.getJobInstance().getJobName(), jobExecution.getStatus(), jobExecution.getJobInstanceId(), jobExecution.getJobParameters());
        }
    }

}
