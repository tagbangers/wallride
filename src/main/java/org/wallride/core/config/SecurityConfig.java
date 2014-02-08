package org.wallride.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.AntPathRequestMatcher;
import org.wallride.core.service.AuthorizedUserDetailsService;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private DataSource dataSource;

	@Override
	protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.userDetailsService(authorizedStaffDetailsService())
			.passwordEncoder(new Md5PasswordEncoder());
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web
			.ignoring()
				.antMatchers("/_admin/resources/**")
				.antMatchers("/_admin/setup**")
				.antMatchers("/_admin/signup**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.antMatcher("/_admin/**")
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
				.requiresChannel()
					.anyRequest().requiresSecure()
					.and()
				.rememberMe()
					.tokenRepository(persistentTokenRepository())
					.and()
				.csrf()
					.disable();
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
