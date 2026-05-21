package com.dolmengi.batch.job.test;

import com.dolmengi.batch.commons.UniqueRunIdIncrementer;
import com.dolmengi.batch.commons.listener.JobListener;
import com.dolmengi.batch.commons.listener.StepListener;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class TestJobConfig {

	private final JobListener jobListener;
	private final StepListener stepListener;

	@Bean
	public Job testJob(JobRepository jobRepository, Step createTestTableStep, Step testReadWriteStep) {
		return new JobBuilder("testJob", jobRepository)
				.incrementer(new UniqueRunIdIncrementer())
				.start(createTestTableStep)
				.next(testReadWriteStep)
				.listener(jobListener)
				.build();
	}

	@Bean
	public Step createTestTableStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			Tasklet createTestTableTasklet) {
		return new StepBuilder("createTestTableStep", jobRepository)
				.tasklet(createTestTableTasklet, transactionManager)
				.listener(stepListener)
				.build();
	}

	@Bean
	public Tasklet createTestTableTasklet(DataSource dataSource) {
		return (contribution, chunkContext) -> {
			JdbcTemplate jdbc = new JdbcTemplate(dataSource);
			jdbc.execute("""
					CREATE TABLE IF NOT EXISTS test_source (
						id BIGINT AUTO_INCREMENT PRIMARY KEY,
						name VARCHAR(100) NOT NULL,
						content VARCHAR(255)
					)
					""");
			jdbc.execute("""
					CREATE TABLE IF NOT EXISTS test_target (
						id BIGINT PRIMARY KEY,
						name VARCHAR(100) NOT NULL,
						content VARCHAR(255)
					)
					""");
			jdbc.update("DELETE FROM test_target");
			jdbc.update("DELETE FROM test_source");
			jdbc.update("INSERT INTO test_source (name, content) VALUES (?, ?)", "item-1", "alpha");
			jdbc.update("INSERT INTO test_source (name, content) VALUES (?, ?)", "item-2", "beta");
			jdbc.update("INSERT INTO test_source (name, content) VALUES (?, ?)", "item-3", "gamma");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Step testReadWriteStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			JdbcCursorItemReader<TestRecord> testReader, ItemProcessor<TestRecord, TestRecord> testProcessor,
			JdbcBatchItemWriter<TestRecord> testWriter) {
		return new StepBuilder("testReadWriteStep", jobRepository)
				.<TestRecord, TestRecord>chunk(2)
				.transactionManager(transactionManager)
				.reader(testReader)
				.processor(testProcessor)
				.writer(testWriter)
				.listener(stepListener)
				.build();
	}

	@Bean
	@StepScope
	public JdbcCursorItemReader<TestRecord> testReader(DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<TestRecord>()
				.name("testReader")
				.dataSource(dataSource)
				.sql("SELECT id, name, content FROM test_source ORDER BY id")
				.rowMapper((rs, rowNum) -> new TestRecord(rs.getLong("id"), rs.getString("name"), rs.getString("content")))
				.build();
	}

	@Bean
	public ItemProcessor<TestRecord, TestRecord> testProcessor() {
		return item -> new TestRecord(item.id(), item.name().toUpperCase(), item.content().toUpperCase());
	}

	@Bean
	public JdbcBatchItemWriter<TestRecord> testWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<TestRecord>()
				.dataSource(dataSource)
				.sql("INSERT INTO test_target (id, name, content) VALUES (:id, :name, :content)")
				.beanMapped()
				.build();
	}

}
