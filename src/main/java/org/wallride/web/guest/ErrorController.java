package org.wallride.web.guest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/{language}/error/{code}")
public class ErrorController {

	@RequestMapping
	public String error(@PathVariable int code, Model model) {
		String viewName = null;
		switch (code) {
			case 403: viewName = "/error/403"; break;
			case 404: viewName = "/error/404"; break;
			default : viewName = "/error/500"; break;
		}
		return viewName;
	}
}
