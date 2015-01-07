package org.wallride.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.wallride.core.job.UpdatePostViewsJobConfig;

@Configuration
@Import({
	UpdatePostViewsJobConfig.class,
})
public class BatchConfig {
	
}
