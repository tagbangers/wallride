package org.wallride.web.controller.admin.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Page;
import org.wallride.core.service.PageService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value="/{language}/pages/bulk-delete", method=RequestMethod.POST)
public class PageBulkDeleteController {

	@Inject
	private PageService pageService;
	
	@Inject
	private MessageSourceAccessor messageSourceAccessor;
	
	private static Logger logger = LoggerFactory.getLogger(PageBulkDeleteController.class);

	@RequestMapping
	public String delete(
			@Valid @ModelAttribute("form") PageBulkDeleteForm form,
			BindingResult errors,
			RedirectAttributes redirectAttributes) {
		if (!form.isConfirmed()) {
			errors.rejectValue("confirmed", "Confirmed");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/pages/index";
		}
		
		Collection<Page> pages = null;
		try {
			pages = pageService.bulkDeletePage(form.buildPageBulkDeleteRequest(), errors);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "redirect:/_admin/{language}/pages/index";
			}
			throw e;
		}
		
		List<String> errorMessages = null;
		if (errors.hasErrors()) {
			errorMessages = new ArrayList<>();
			for (ObjectError error : errors.getAllErrors()) {
				errorMessages.add(messageSourceAccessor.getMessage(error));
			}
		}
		
		redirectAttributes.addFlashAttribute("deletedPages", pages);
		redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		return "redirect:/_admin/{language}/pages/index";
	}
}
