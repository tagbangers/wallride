package org.wallride.admin.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages="org.wallride.admin", excludeFilters={ @Filter(Configuration.class)} )
public class AdminConfig {

}