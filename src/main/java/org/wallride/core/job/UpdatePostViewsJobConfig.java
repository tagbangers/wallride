package org.wallride.core.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

@Configuration
public class UpdatePostViewsJobConfig {

	@Inject
	private JobBuilderFactory jobBuilders;
	@Inject
	private StepBuilderFactory stepBuilders;

	@Inject
	private UpdatePostViewsItemReader updatePostViewsItemReader;
	@Inject
	private UpdatePostViewsItemWriter updatePostViewsItemWriter;

	@Bean
	public Job updatePostViewsJob() {
		return jobBuilders.get("updatePostViewsJob")
				.start(updatePostViewsStep())
				.build();
	}

	public Step updatePostViewsStep() {
		return stepBuilders.get("updatePostViewsStep")
				.chunk(10)
				.reader((ItemReader) updatePostViewsItemReader)
				.writer((ItemWriter) updatePostViewsItemWriter)
				.build();
	}
}
