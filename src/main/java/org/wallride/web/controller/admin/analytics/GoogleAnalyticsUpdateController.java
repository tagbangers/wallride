package org.wallride.web.controller.admin.analytics;

import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.GoogleAnalytics;
import org.wallride.core.service.BlogService;
import org.wallride.core.service.GoogleAnalyticsException;
import org.wallride.core.service.GoogleAnalyticsUpdateRequest;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/analytics/edit")
public class GoogleAnalyticsUpdateController {

	@Inject
	private BlogService blogService;

	public static final String FORM_MODEL_KEY = "form";
	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	@ModelAttribute(FORM_MODEL_KEY)
	public GoogleAnalyticsUpdateForm setupGoogleAnalyticsUpdateForm() {
		return new GoogleAnalyticsUpdateForm();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) {
		GoogleAnalyticsUpdateForm form = new GoogleAnalyticsUpdateForm();
		model.addAttribute(FORM_MODEL_KEY, form);
		return edit(model);
	}

	@RequestMapping(method = RequestMethod.GET, params = "step.edit")
	public String edit(Model model) {
		return "/analytics/edit";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String update(
			@PathVariable String language,
			@Validated @ModelAttribute(FORM_MODEL_KEY) GoogleAnalyticsUpdateForm form,
			BindingResult errors,
			@AuthenticationPrincipal AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, form);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, errors);

		if (errors.hasErrors()) {
			return "redirect:/_admin/{language}/analytics/edit?step.edit";
		}

		GoogleAnalyticsUpdateRequest request = new GoogleAnalyticsUpdateRequest();
		request.setBlogId(Blog.DEFAULT_ID);
		request.setTrackingId(form.getTrackingId());
		request.setProfileId(form.getProfileId());
		request.setCustomDimensionIndex(form.getCustomDimensionIndex());
		request.setServiceAccountId(form.getServiceAccountId());
		request.setServiceAccountP12File(form.getServiceAccountP12File());

		GoogleAnalytics updatedGoogleAnalytics;
		try {
			updatedGoogleAnalytics = blogService.updateGoogleAnalytics(request);
		} catch (GoogleAnalyticsException e) {
			errors.reject("GoogleAnalytics");
			return "redirect:/_admin/{language}/analytics/edit?step.edit";
		}

		redirectAttributes.getFlashAttributes().clear();
		redirectAttributes.addFlashAttribute("updatedGoogleAnalytics", updatedGoogleAnalytics);
		return "redirect:/_admin/{language}/analytics";
	}
}
