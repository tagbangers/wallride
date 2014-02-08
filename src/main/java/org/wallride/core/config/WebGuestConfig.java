package org.wallride.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.web.MediaHttpRequestHandler;
import org.wallride.core.web.admin.AuthorizedUserMethodArgumentResolver;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.CustomThymeleafDialect;
import org.wallride.core.support.Settings;
import org.wallride.core.web.DefaultModelAttributeInterceptor;
import org.wallride.core.web.PathVariableLocaleResolver;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
//@EnableWebMvc
//public class WebConfig extends WebMvcConfigurerAdapter {
public class WebGuestConfig extends WebMvcConfigurationSupport {

	@Inject
	private DefaultModelAttributeInterceptor defaultModelAttributeInterceptor;

	@Inject
	private CustomThymeleafDialect customThymeleafDialect;

	@Inject
	private Settings settings;

	@Inject
	private MediaRepository mediaRepository;

	@Inject
	private Environment environment;

	@Inject
	private ResourceLoader resourceLoader;

	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();

		UrlPathHelper customUrlPathHelper = new UrlPathHelper() {
			@Override
			public String getLookupPathForRequest(HttpServletRequest request) {
				String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
				if (defaultLanguage != null) {
					String[] languages = StringUtils.commaDelimitedListToStringArray(settings.readSettingAsString(Setting.Key.LANGUAGES));
//					String[] languages = StringUtils.split(settingService.readSettingAsString(Setting.Key.LANGUAGES), ",");
					String path = super.getLookupPathForRequest(request);
					boolean languagePath = false;
					for (String language : languages) {
						if (path.startsWith("/" + language + "/")) {
							languagePath = true;
							break;
						}
					}
					if (!languagePath) {
						path = "/" + defaultLanguage + path;
					}
					return path;
				}
				else {
					return super.getLookupPathForRequest(request);
				}
			}
		};
		handlerMapping.setUrlPathHelper(customUrlPathHelper);

		handlerMapping.setOrder(0);
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
		registry.addResourceHandler("/resources/**").addResourceLocations("/WEB-INF/resources/blog/");
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
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver();
	}

	// additional webmvc-related beans

	@Bean
	public SimpleUrlHandlerMapping mediaUrlHandlerMapping() {
		MediaHttpRequestHandler mediaHttpRequestHandler = new MediaHttpRequestHandler();
		mediaHttpRequestHandler.setMediaRepository(mediaRepository);
		mediaHttpRequestHandler.setResourceLoader(resourceLoader);
		mediaHttpRequestHandler.setEnvironment(environment);
		mediaHttpRequestHandler.setCacheSeconds(86400);

		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<String, HttpRequestHandler>();
		urlMap.put("/{language}/media/{key}", mediaHttpRequestHandler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(Integer.MAX_VALUE -1);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

	@Bean
	public ServletContextTemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix(environment.getRequiredProperty("template.blog.path"));
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");
		// NB, selecting HTML5 as the template mode.
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(environment.getRequiredProperty("template.blog.cache", Boolean.class));
		return resolver;

	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(templateResolver());

		Set<IDialect> dialects = new HashSet<>();
		dialects.add(customThymeleafDialect);
		engine.setAdditionalDialects(dialects);
		return engine;
	}

	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(templateEngine());
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
