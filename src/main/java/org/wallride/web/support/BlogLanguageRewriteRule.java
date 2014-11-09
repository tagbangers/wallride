package org.wallride.web.support;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.UrlPathHelper;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BlogLanguageRewriteRule extends RewriteRule {

//	private BlogService blogService;
//
//	@Override
//	public boolean initialise(ServletContext servletContext) {
//		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
//		blogService = context.getBean(BlogService.class);
//		return super.initialise(servletContext);
//	}

	@Override
	public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response) {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
		BlogService blogService = context.getBean(BlogService.class);
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);

		UrlPathHelper urlPathHelper = new UrlPathHelper();
		String path = urlPathHelper.getLookupPathForRequest(request);

		BlogLanguage matchedBlogLanguage = null;
		for (BlogLanguage blogLanguage : blog.getLanguages()) {
			if (path.startsWith("/" + blogLanguage.getLanguage() + "/")) {
				matchedBlogLanguage = blogLanguage;
				break;
			}
		}

		if (matchedBlogLanguage == null) {
			return null;
		}

		return new BlogLanguageRewriteMatch(matchedBlogLanguage);
	}
}
