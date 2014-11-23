package org.wallride.web.support;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import java.util.Locale;

public class ExtendedThymeleafViewResolver extends ThymeleafViewResolver {

	@Override
	protected View createView(final String viewName, final Locale locale) throws Exception {
		View view = super.createView(viewName, locale);
		if (view instanceof RedirectView) {
			RedirectView redirectView = (RedirectView) view;
			redirectView.setApplicationContext(getApplicationContext());
			redirectView.setServletContext(getServletContext());
		}
		return view;
	}
}
