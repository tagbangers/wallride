package org.wallride.web.controller.admin.analytics;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/{language}/analytics")
public class GoogleAnalyticsDeleteController {

	@RequestMapping(method = RequestMethod.DELETE)
	public String delete(
			@PathVariable String language,
			RedirectAttributes redirectAttributes) {
		return "redirect:/_admin/{language}/google-analytics";
	}
}
