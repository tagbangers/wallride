package org.wallride.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UrlPathHelper;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.wallride.core.domain.BlogLanguage;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BlogLanguageRewriteMatch extends RewriteMatch {

	private BlogLanguage blogLanguage;

	private static Logger logger = LoggerFactory.getLogger(BlogLanguageRewriteMatch.class);

	public BlogLanguageRewriteMatch(BlogLanguage blogLanguage) {
		this.blogLanguage = blogLanguage;
	}

	@Override
	public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		String originalPath = urlPathHelper.getLookupPathForRequest(request);

		String rewritePath = originalPath.replaceAll("^/" + blogLanguage.getLanguage() + "/", "/");
		logger.debug(originalPath + " => " + rewritePath);

		request.setAttribute(BlogLanguageMethodArgumentResolver.BLOG_LANGUAGE_ATTRIBUTE, blogLanguage);

		RequestDispatcher rd = request.getRequestDispatcher(urlPathHelper.getServletPath(request) + rewritePath);
		rd.forward(request, response);
		return true;
	}
}
