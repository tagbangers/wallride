package org.wallride.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.CategoryService;
import org.wallride.core.service.PageService;
import org.wallride.core.support.CustomThymeleafDialect;
import org.wallride.web.support.AuthorizedUserMethodArgumentResolver;
import org.wallride.web.controller.guest.page.PageDescribeController;
import org.wallride.web.support.*;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
@ComponentScan(basePackages= "org.wallride.web.controller.guest", excludeFilters={ @ComponentScan.Filter(Configuration.class)} )
public class WebGuestConfig extends WebMvcConfigurationSupport {

	@Inject
	private MessageCodesResolver messageCodesResolver;
	@Inject
	private SpringResourceResourceResolver springResourceResourceResolver;

	@Inject
	private CustomThymeleafDialect customThymeleafDialect;

	@Inject
	private BlogService blogService;

	@Inject
	private CategoryService categoryService;

	@Inject
	private PageService pageService;

	@Inject
	private ResourceLoader resourceLoader;

	@Inject
	private Environment environment;

	@Resource
	private MediaRepository mediaRepository;

	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();

//		handlerMapping.setUrlPathHelper(new LanguageUrlPathHelper(blogService));
		handlerMapping.setDefaultHandler(new PageDescribeController(blogService, pageService));

		handlerMapping.setOrder(Integer.MAX_VALUE);
		handlerMapping.setInterceptors(getInterceptors());
		handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());
		return handlerMapping;
//		return super.requestMappingHandlerMapping();    //To change body of overridden methods use File | Settings | File Templates.
	}

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

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/guest/");
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
		BlogLanguageMethodArgumentResolver blogLanguageMethodArgumentResolver = new BlogLanguageMethodArgumentResolver();
		blogLanguageMethodArgumentResolver.setBlogService(blogService);

		argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
		argumentResolvers.add(new AuthorizedUserMethodArgumentResolver());
		argumentResolvers.add(blogLanguageMethodArgumentResolver);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(defaultModelAttributeInterceptor());
		registry.addInterceptor(setupRedirectInterceptor());
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	// additional webmvc-related beans

	@Bean
	public SimpleUrlHandlerMapping mediaUrlHandlerMapping() {
		MediaHttpRequestHandler mediaHttpRequestHandler = new MediaHttpRequestHandler();
		mediaHttpRequestHandler.setBlogService(blogService);
		mediaHttpRequestHandler.setMediaRepository(mediaRepository);
		mediaHttpRequestHandler.setResourceLoader(resourceLoader);
		mediaHttpRequestHandler.setCacheSeconds(86400);

		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<>();
		urlMap.put("/media/{key}", mediaHttpRequestHandler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(0);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

//	@Bean
//	public SimpleUrlHandlerMapping pageUrlHandlerMapping() {
//		PageHttpRequestHandler pageHttpRequestHandler = new PageHttpRequestHandler();
//
//		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<>();
//		urlMap.put("/{language}/{code}", pageHttpRequestHandler);
//
//		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
//		handlerMapping.setOrder(2);
//		handlerMapping.setUrlMap(urlMap);
//		return handlerMapping;
//	}

	@Bean
	public DefaultModelAttributeInterceptor defaultModelAttributeInterceptor() {
		DefaultModelAttributeInterceptor defaultModelAttributeInterceptor = new DefaultModelAttributeInterceptor();
		defaultModelAttributeInterceptor.setBlogService(blogService);
		defaultModelAttributeInterceptor.setCategoryService(categoryService);
		defaultModelAttributeInterceptor.setPageService(pageService);
		return defaultModelAttributeInterceptor;
	}

	@Bean
	public SetupRedirectInterceptor setupRedirectInterceptor() {
		SetupRedirectInterceptor setupRedirectInterceptor = new SetupRedirectInterceptor();
		setupRedirectInterceptor.setBlogService(blogService);
		return setupRedirectInterceptor;
	}

	@Bean
	public TemplateResolver templateResolver() {
		TemplateResolver resolver = new TemplateResolver();
		resolver.setResourceResolver(springResourceResourceResolver);
		resolver.setPrefix(environment.getRequiredProperty("template.guest.path"));
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		// NB, selecting HTML5 as the template mode.
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(environment.getRequiredProperty("template.guest.cache", Boolean.class));
		return resolver;

	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(templateResolver());

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(new SpringSecurityDialect());
		dialects.add(customThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}

	@Bean
	public BeanNameViewResolver beanNameViewResolver() {
		BeanNameViewResolver viewResolver = new BeanNameViewResolver();
		viewResolver.setOrder(1);
		return viewResolver;
	}

	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver viewResolver = new ExtendedThymeleafViewResolver();
		viewResolver.setTemplateEngine(templateEngine());
		viewResolver.setOrder(2);
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
	public LocaleResolver localeResolver() {
		BlogLanguageLocaleResolver blogLanguageLocaleResolver = new BlogLanguageLocaleResolver();
		blogLanguageLocaleResolver.setBlogService(blogService);
		return blogLanguageLocaleResolver;
	}

	@Bean(name = "atomFeedView")
	public View atomFeedView() {
		AtomFeedView view = new AtomFeedView();
		view.setBlogService(blogService);
		return view;
	}

	@Bean(name = "rssFeedView")
	public View rssFeedView() {
		RssFeedView view = new RssFeedView();
		view.setBlogService(blogService);
		return view;
	}

	@Bean
	public RequestDataValueProcessor requestDataValueProcessor() {
		return new BlogLanguageDataValueProcessor();
	}
}
