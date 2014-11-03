package org.wallride.web.controller.admin.analytics;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wallride.core.domain.Blog;
import org.wallride.core.service.BlogService;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/analytics")
public class GoogleAnalyticsIndexController {

	@Inject
	private BlogService blogService;

	@RequestMapping(method = RequestMethod.GET)
	public String describe(Model model) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		model.addAttribute("googleAnalytics", blog.getGoogleAnalytics());
		return "analytics/index";
	}
}
