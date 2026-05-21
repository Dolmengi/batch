package com.dolmengi.batch.job.demo;

import com.dolmengi.batch.commons.UniqueRunIdIncrementer;
import com.dolmengi.batch.commons.listener.JobListener;
import com.dolmengi.batch.commons.listener.StepListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DemoJobConfig {

	private final EntityManagerFactory entityManagerFactory;
	private final JobListener jobListener;
	private final StepListener stepListener;

	@Bean
	public Job demoJob(JobRepository jobRepository, Step initDemoDataStep, Step demoProcessStep) {
		return new JobBuilder("demoJob", jobRepository)
				.incrementer(new UniqueRunIdIncrementer())
				.start(initDemoDataStep)
				.next(demoProcessStep)
				.listener(jobListener)
				.build();
	}

	@Bean
	public Step initDemoDataStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("initDemoDataStep", jobRepository)
				.tasklet(initDemoDataTasklet(), transactionManager)
				.listener(stepListener)
				.build();
	}

	@Bean
	public Tasklet initDemoDataTasklet() {
		return (contribution, chunkContext) -> {
			EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
			em.createQuery("DELETE FROM DemoTarget").executeUpdate();
			em.createQuery("DELETE FROM DemoSource").executeUpdate();

			for (int i = 1; i <= 5; i++) {
				DemoSource src = new DemoSource();
				src.setName("item-" + i);
				src.setAmount(i * 100);
				em.persist(src);
			}
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Step demoProcessStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("demoProcessStep", jobRepository)
				.<DemoSource, DemoTarget>chunk(3)
				.transactionManager(transactionManager)
				.reader(demoReader())
				.processor(demoProcessor())
				.writer(demoWriter())
				.listener(stepListener)
				.build();
	}

	@Bean
	@StepScope
	public JpaPagingItemReader<DemoSource> demoReader() {
		return new JpaPagingItemReaderBuilder<DemoSource>()
				.name("demoReader")
				.entityManagerFactory(entityManagerFactory)
				.queryString("SELECT d FROM DemoSource d ORDER BY d.id")
				.pageSize(3)
				.build();
	}

	@Bean
	public ItemProcessor<DemoSource, DemoTarget> demoProcessor() {
		return source -> {
			DemoTarget target = new DemoTarget();
			target.setName(source.getName().toUpperCase());
			target.setAmount(source.getAmount() * 2);
			return target;
		};
	}

	@Bean
	public JpaItemWriter<DemoTarget> demoWriter() {
		return new JpaItemWriterBuilder<DemoTarget>()
				.entityManagerFactory(entityManagerFactory)
				.build();
	}

}
