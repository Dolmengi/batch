package com.dolmengi.batch.config;

import com.dolmengi.batch.commons.listener.JobListener;
import com.dolmengi.batch.commons.listener.SleepListener;
import com.dolmengi.batch.commons.listener.StepListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchListenerConfig {

	@Bean
	public JobListener jobListener() {
		return new JobListener();
	}

	@Bean
	public StepListener stepListener() {
		return new StepListener();
	}

	@Bean
	public SleepListener sleepListener() {
		return new SleepListener();
	}

}
