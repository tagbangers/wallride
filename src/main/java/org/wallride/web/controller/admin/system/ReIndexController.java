package org.wallride.web.controller.admin.system;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.service.SystemService;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/system/re-index")
public class ReIndexController {

	private static final int BATCH_SIZE = 10;

	@Inject
	private SystemService systemService;

	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		return "/system/re-index";
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@RequestMapping(method = RequestMethod.POST)
	public String reIndex(
			@PathVariable String language,
			RedirectAttributes redirectAttributes) throws Exception {
//		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
//		fullTextEntityManager.createIndexer().start();

		systemService.reIndex();

		redirectAttributes.addFlashAttribute("reIndex", true);
		redirectAttributes.addAttribute("language", language);
		return "redirect:/_admin/{language}/system/re-index";
	}

}
