package org.wallride.blog.web;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Setting;
import org.wallride.core.support.Settings;

import javax.inject.Inject;

@Controller @Lazy
@RequestMapping("/")
public class IndexController {

	@Inject
	private Settings settings;

	@RequestMapping
	public String index(RedirectAttributes redirectAttributes) {
		String defaultLanguage = settings.readSettingAsString(Setting.Key.DEFAULT_LANGUAGE);
		redirectAttributes.addAttribute("language", defaultLanguage);
		return "redirect:/{language}/";
	}
}
