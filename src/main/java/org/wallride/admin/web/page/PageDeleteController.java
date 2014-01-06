package org.wallride.admin.web.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wallride.admin.service.PageService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.ValidationException;

@Controller
@RequestMapping(value="/pages/delete", method=RequestMethod.POST)
public class PageDeleteController {
	
	private static Logger logger = LoggerFactory.getLogger(PageDeleteController.class); 
	
	@Inject
	private PageService pageService;
	
	@RequestMapping
	public String delete(@Valid @ModelAttribute("form") PageDeleteForm form, BindingResult errors) {
		if (!form.isConfirmed()) {
			errors.rejectValue("confirmed", "Confirmed");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "/page/delete";
		}
		
		try {
			pageService.deletePage(form, errors);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "/page/delete";
			}
			throw e;
		}
		
		return "/page/delete";
	}
}
