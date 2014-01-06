package org.wallride.blog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages="org.wallride.blog", excludeFilters={ @Filter(Configuration.class)} )
public class BlogConfig {
	
}