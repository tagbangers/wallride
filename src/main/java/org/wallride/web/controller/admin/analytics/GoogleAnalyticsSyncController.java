package org.wallride.web.controller.admin.analytics;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.PostService;

import javax.inject.Inject;

@Controller
@RequestMapping("/analytics/sync")
public class GoogleAnalyticsSyncController {

	@Inject
	private PostService postService;

	@RequestMapping(method = RequestMethod.POST)
	public String sync(
			@PathVariable String language,
			RedirectAttributes redirectAttributes) {
		postService.syncGoogleAnalytics();
		return "redirect:/_admin/{language}/analytics";
	}
}
