package com.dolmengi.batch.job.sample;

import com.dolmengi.batch.commons.UniqueRunIdIncrementer;
import com.dolmengi.batch.commons.listener.JobListener;
import com.dolmengi.batch.commons.listener.SleepListener;
import com.dolmengi.batch.commons.listener.StepListener;
import com.dolmengi.batch.commons.listener.WriterListener;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SampleJobConfig {

	private final JobListener jobListener;
	private final StepListener stepListener;
	private final SleepListener sleepListener;

	@Bean
	public Job sampleJob(JobRepository jobRepository, Step sampleStep) {
		return new JobBuilder("sampleJob", jobRepository)
				.incrementer((new UniqueRunIdIncrementer()))
				.start(sampleStep)
				.listener(jobListener)
				.build();
	}

	@Bean
	public Step sampleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, WriterListener writerListener) {
		return new StepBuilder("sampleStep", jobRepository)
				.<String, String>chunk(2)
				.transactionManager(transactionManager)
				.reader(sampleReader())
				.processor(sampleProcessor())
				.writer(sampleWriter())
				.listener(stepListener)
				.listener(sleepListener)
				.listener(writerListener)
				.build();
	}

	@Bean
	@StepScope
	public WriterListener writerListener(@Value("#{stepExecution}") StepExecution stepExecution) {
		return new WriterListener(stepExecution);
	}

	@Bean
	public ItemReader<String> sampleReader() {
		return new ListItemReader<>(List.of("hello", "spring", "batch", "world"));
	}

	@Bean
	public ItemProcessor<String, String> sampleProcessor() {
		return String::toUpperCase;
	}

	@Bean
	public ItemWriter<String> sampleWriter() {
		return items -> items.forEach(item -> System.out.println("[sampleWriter] " + item));
	}

}
