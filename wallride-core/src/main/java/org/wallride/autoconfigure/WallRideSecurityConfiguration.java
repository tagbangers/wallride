/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.channel.ChannelProcessor;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.wallride.service.AuthorizedUserDetailsService;
import org.wallride.support.ProxyInsecureChannelProcessor;
import org.wallride.support.ProxySecureChannelProcessor;
import org.wallride.web.support.BlogLanguageRedirectStrategy;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WallRideSecurityConfiguration {

	@Autowired
	private DataSource dataSource;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// @formatter:off
		auth
			.userDetailsService(authorizedUserDetailsService())
			.passwordEncoder(new StandardPasswordEncoder());
		// @formatter:on
	}

	@Configuration
	@Order(1)
	public static class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private AccessDecisionManager accessDecisionManager;
//		@Autowired
//		private SecurityExpressionHandler securityExpressionHandler;
		@Autowired
		private PersistentTokenRepository persistentTokenRepository;
		@Autowired
		private Environment environment;

		@Override
		public void configure(WebSecurity web) throws Exception {
			// @formatter:off
			web
				.ignoring()
					.antMatchers("/_admin/resources/**")
					.antMatchers("/_admin/webjars/**")
					.antMatchers("/_admin/setup**")
					.antMatchers("/_admin/signup**");
			// @formatter:on
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.antMatcher("/_admin/**")
				.authorizeRequests()
					.accessDecisionManager(accessDecisionManager)
//		            .expressionHandler(securityExpressionHandler)
					.antMatchers("/_admin/**").hasRole("ADMIN")
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
					.tokenRepository(persistentTokenRepository)
					.and()
				.headers()
					.frameOptions().disable()
					.cacheControl().disable()
					.and()
				.csrf()
					.disable()
				.exceptionHandling()
					.accessDeniedPage("/_admin/login");
			if (environment.getProperty("security.require-ssl", Boolean.class, false)) {
				List<ChannelProcessor> channelProcessors = new ArrayList<>();
				channelProcessors.add(new ProxySecureChannelProcessor());
				channelProcessors.add(new ProxyInsecureChannelProcessor());

				http.requiresChannel()
					.channelProcessors(channelProcessors)
					.anyRequest().requiresSecure();
			}
			// @formatter:on
		}
	}

	@Configuration
	@Order(2)
	public static class GuestSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private AccessDecisionManager accessDecisionManager;
//		@Autowired
//		private SecurityExpressionHandler securityExpressionHandler;
		@Autowired
		private PersistentTokenRepository persistentTokenRepository;
		@Autowired
		private Environment environment;

		@Override
		public void configure(WebSecurity web) throws Exception {
			// @formatter:off
			web
				.ignoring()
					.antMatchers("/resources/**")
					.antMatchers("/webjars/**");
			// @formatter:on
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			RedirectStrategy redirectStrategy = new BlogLanguageRedirectStrategy();

			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successHandler.setRedirectStrategy(redirectStrategy);
			successHandler.setDefaultTargetUrl("/");

			SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/login?failed");
			failureHandler.setRedirectStrategy(redirectStrategy);

			SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
			logoutSuccessHandler.setRedirectStrategy(redirectStrategy);
			logoutSuccessHandler.setDefaultTargetUrl("/");

			// @formatter:off
			http.antMatcher("/**")
				.authorizeRequests()
					.accessDecisionManager(accessDecisionManager)
//		            .expressionHandler(securityExpressionHandler)
					.antMatchers("/settings/**").hasRole("VIEWER")
					.antMatchers("/comments/**").hasRole("VIEWER")
					.and()
				.formLogin()
					.loginPage("/login").permitAll()
					.loginProcessingUrl("/login")
					.successHandler(successHandler)
					.failureHandler(failureHandler)
					.and()
				.logout()
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
					.logoutSuccessHandler(logoutSuccessHandler)
					.and()
				.rememberMe()
					.tokenRepository(persistentTokenRepository)
					.and()
				.headers()
					.frameOptions().disable()
					.cacheControl().disable()
					.and()
				.csrf()
					.disable()
				.exceptionHandling()
					.accessDeniedPage("/login");
			if (environment.getProperty("security.require-ssl", Boolean.class, false)) {
				List<ChannelProcessor> channelProcessors = new ArrayList<>();
				channelProcessors.add(new ProxySecureChannelProcessor());
				channelProcessors.add(new ProxyInsecureChannelProcessor());

				http.requiresChannel()
					.channelProcessors(channelProcessors)
					.anyRequest().requiresSecure();
			}
			// @formatter:on
		}
	}

	@Bean
	public UserDetailsService authorizedUserDetailsService() {
		return new AuthorizedUserDetailsService();
	}

	@Bean
	public AffirmativeBased accessDecisionManager() {
		List<AccessDecisionVoter<?>> accessDecisionVoters = new ArrayList<>();
		accessDecisionVoters.add(roleVoter());
		accessDecisionVoters.add(webExpressionVoter());

		AffirmativeBased accessDecisionManager = new AffirmativeBased(accessDecisionVoters);
		return accessDecisionManager;
	}

	@Bean
	public WebExpressionVoter webExpressionVoter() {
		WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
		webExpressionVoter.setExpressionHandler(webSecurityExpressionHandler());
		return webExpressionVoter;
	}

	@Bean
	public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
		DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
		defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
		return defaultWebSecurityExpressionHandler;
	}

	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
		hierarchy.setHierarchy("ROLE_ADMIN > ROLE_VIEWER");
		return hierarchy;
	}

	@Bean
	public RoleVoter roleVoter() {
		return new RoleHierarchyVoter(roleHierarchy());
	}

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
		repository.setDataSource(dataSource);
		return repository;
	}
}
