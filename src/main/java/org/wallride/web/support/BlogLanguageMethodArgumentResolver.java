package org.wallride.web.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.BlogService;

public class BlogLanguageMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private BlogService blogService;

	public static final String BLOG_LANGUAGE_ATTRIBUTE = BlogLanguageMethodArgumentResolver.class.getName() + ".LANGUAGE";

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return BlogLanguage.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory)
			throws Exception {
		BlogLanguage blogLanguage = (BlogLanguage) webRequest.getAttribute(BLOG_LANGUAGE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		if (blogLanguage != null) {
			return blogLanguage;
		}
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		return blog.getLanguage(blog.getDefaultLanguage());
	}
}
