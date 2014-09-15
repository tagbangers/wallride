package org.wallride.web.controller.guest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Blog;
import org.wallride.core.service.BlogService;

import javax.inject.Inject;

@Controller
@RequestMapping("/")
public class IndexController {

	@Inject
	private BlogService blogService;

	@RequestMapping
	public String index(RedirectAttributes redirectAttributes) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String defaultLanguage = blog.getDefaultLanguage();
		redirectAttributes.addAttribute("language", defaultLanguage);
		return "redirect:/{language}/";
	}
}
