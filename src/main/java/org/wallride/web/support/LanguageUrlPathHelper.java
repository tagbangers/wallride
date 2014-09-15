package org.wallride.web.support;

import org.springframework.web.util.UrlPathHelper;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;

public class LanguageUrlPathHelper extends UrlPathHelper {

	private BlogService blogService;

	public LanguageUrlPathHelper(BlogService blogService) {
		this.blogService = blogService;
	}

	@Override
	public String getLookupPathForRequest(HttpServletRequest request) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String defaultLanguage = blog.getDefaultLanguage();
		if (defaultLanguage != null) {
			String path = super.getLookupPathForRequest(request);
			boolean languagePath = false;
			for (BlogLanguage blogLanguage : blog.getLanguages()) {
				if (path.startsWith("/" + blogLanguage.getLanguage() + "/")) {
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
}
