package org.wallride.web.support;

import org.springframework.security.web.RedirectStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BlogLanguageRedirectStrategy implements RedirectStrategy {

	@Override
	public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
		String redirectUrl = request.getContextPath() + url;

		BlogLanguageDataValueProcessor processor = new BlogLanguageDataValueProcessor();
		redirectUrl = processor.processUrl(request, redirectUrl);

		redirectUrl = response.encodeRedirectURL(redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}
