package org.wallride.web.controller.admin.system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/{language}/system/index")
public class SystemIndexController {

	@RequestMapping
	public String index(Model model) {
		model.addAttribute("system", System.getProperties());
		return "system/index";
	}
}
