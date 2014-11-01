package org.wallride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessor;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.AntPathRequestMatcher;
import org.wallride.core.service.AuthorizedUserDetailsService;
import org.wallride.core.support.ProxyInsecureChannelProcessor;
import org.wallride.core.support.ProxySecureChannelProcessor;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private DataSource dataSource;

	@Inject
	private Environment environment;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// @formatter:off
		auth
			.userDetailsService(authorizedStaffDetailsService())
			.passwordEncoder(new StandardPasswordEncoder());
		// @formatter:on
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		// @formatter:off
		web
			.ignoring()
				.antMatchers("/_admin/resources/**")
				.antMatchers("/_admin/setup**")
				.antMatchers("/_admin/signup**");
		// @formatter:on
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.antMatcher("/_admin/**")
			.authorizeRequests()
				.antMatchers("/_admin/**").hasRole("USER")
				.and()
			.formLogin()
				.loginPage("/_admin/login").permitAll()
				.loginProcessingUrl("/_admin/login")
				.defaultSuccessUrl("/_admin/")
				.failureUrl("/_admin/login?failed")
				.and()
			.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/_admin/logout", "GET"))
				.logoutSuccessUrl("/_admin/login")
				.and()
			.rememberMe()
				.tokenRepository(persistentTokenRepository())
				.and()
			.headers()
				.frameOptions().disable()
			.csrf()
				.disable();
		//TODO
//		if (environment.getProperty("security.admin.force.ssl", Boolean.class, false)) {
//			List<ChannelProcessor> channelProcessors = new ArrayList<>();
//			channelProcessors.add(new ProxySecureChannelProcessor());
//			channelProcessors.add(new ProxyInsecureChannelProcessor());
//
//			http.requiresChannel()
//				.channelProcessors(channelProcessors)
//				.anyRequest().requiresSecure();
//		}
		// @formatter:on
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public UserDetailsService authorizedStaffDetailsService() {
		return new AuthorizedUserDetailsService();
	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
		repository.setDataSource(dataSource);
		return repository;
	}

}
