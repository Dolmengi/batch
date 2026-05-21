package com.dolmengi.batch.commons;

import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;

public class UniqueRunIdIncrementer extends RunIdIncrementer {

	private static final String RUN_ID = "run.id";
	private static final Long DEFAULT_VALUE = 0L;

	@Override
	public JobParameters getNext(JobParameters parameters) {
		JobParameters params = parameters != null ? parameters : new JobParameters();
		Long current = params.getLong(RUN_ID, DEFAULT_VALUE);
		Long next = current != null ? current + 1L : DEFAULT_VALUE;

		return new JobParametersBuilder().addLong(RUN_ID, next).toJobParameters();
	}

}
