package org.wallride.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.wallride.admin.web.AuthorizedUserMethodArgumentResolver;
import org.wallride.core.support.CustomThymeleafDialect;
import org.wallride.core.web.DefaultModelAttributeInterceptor;
import org.wallride.core.web.PathVariableLocaleResolver;
import org.wallride.core.web.SetupCheckInterceptor;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

	@Inject
	private Environment environment;

	@Inject
	private DefaultModelAttributeInterceptor defaultModelAttributeInterceptor;

	@Inject
	private SetupCheckInterceptor setupCheckInterceptor;

	@Inject
	private CustomThymeleafDialect customThymeleafDialect;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//		converters.add(new FormHttpMessageConverter());

		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new ResourceHttpMessageConverter());

		MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
		objectMapper.setDateFormat(dateFormat);
		jackson.setObjectMapper(objectMapper);
		converters.add(jackson);
	}

//	@Override
//	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//		exceptionResolvers.add(new ExceptionHandlerExceptionResolver());
//	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/WEB-INF/resources/admin/");
		registry.setOrder(Integer.MIN_VALUE);
	}
	
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addFormatter(new Formatter<String>() {
			@Override
			public String print(String object, Locale locale) {
				return (!object.equals("") ? object : null);
			}

			@Override
			public String parse(String text, Locale locale) throws ParseException {
				String value = StringUtils.trimWhitespace(text);
				return Normalizer.normalize(value, Normalizer.Form.NFKC);
			}
		});
	}
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new AuthorizedUserMethodArgumentResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(defaultModelAttributeInterceptor);
		registry.addInterceptor(setupCheckInterceptor).addPathPatterns("/setup**");
	}
	
	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver();
	}
	
	// additional webmvc-related beans

	@Bean(name="emailTemplateResolver")
	public TemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("mail/");
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		// NB, selecting HTML5 as the template mode.
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(false);
		resolver.setOrder(1);
		return resolver;
	}

	@Bean(name="adminTemplateResolver")
	public ServletContextTemplateResolver adminTemplateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix(environment.getRequiredProperty("admin.template.path"));
		resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
		// NB, selecting HTML5 as the template mode.
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(false);
		resolver.setOrder(2);
		return resolver;
	}

	@Bean(name="blogTemplateResolver")
	public ServletContextTemplateResolver blogTemplateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix(environment.getRequiredProperty("blog.template.path"));
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(false);
		resolver.setOrder(2);
		return resolver;
	}

	@Bean(name="adminTemplateEngine")
	public SpringTemplateEngine adminTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		Set<TemplateResolver> resolvers = new HashSet<>();
		resolvers.add(emailTemplateResolver());
		resolvers.add(adminTemplateResolver());
		engine.setTemplateResolvers(resolvers);

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(customThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}

	@Bean(name="blogTemplateEngine")
	public SpringTemplateEngine blogTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		Set<TemplateResolver> resolvers = new HashSet<>();
		resolvers.add(blogTemplateResolver());
		engine.setTemplateResolvers(resolvers);

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(customThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}

	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(adminTemplateEngine());
		viewResolver.setOrder(1);
		viewResolver.setViewNames(new String[] { "*" });
		viewResolver.setCache(false);
		viewResolver.setCharacterEncoding("UTF-8");
		viewResolver.setContentType("text/html; charset=UTF-8");
		return viewResolver;
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}
	
	@Bean
	public MessageCodesResolver messageCodesResolver() {
		DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
		resolver.setPrefix("validation.");
		return resolver;
	}

	@Bean
	public LocaleResolver localeResolver() {
		return new PathVariableLocaleResolver();
	}
}
