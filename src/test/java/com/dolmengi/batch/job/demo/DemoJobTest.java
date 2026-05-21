package com.dolmengi.batch.job.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class DemoJobTest {

	@Autowired
	@Qualifier("demoJob")
	private Job demoJob;

	@Autowired
	private JobOperatorTestUtils jobOperatorTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jobOperatorTestUtils.setJob(demoJob);
		jobRepositoryTestUtils.removeJobExecutions();
	}

	@Test
	void demoJob_copiesAndTransformsAllSources() throws Exception {
		JobExecution jobExecution = jobOperatorTestUtils.startJob(jobOperatorTestUtils.getUniqueJobParameters());

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM demo_target", Integer.class)).isEqualTo(5);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT name FROM demo_target WHERE amount = ?",
				String.class,
				200)).isEqualTo("ITEM-1");
		assertThat(jdbcTemplate.queryForObject(
				"SELECT amount FROM demo_target WHERE name = ?",
				Integer.class,
				"ITEM-5")).isEqualTo(1000);
	}

}
