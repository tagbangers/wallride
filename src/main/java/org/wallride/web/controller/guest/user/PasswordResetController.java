package org.wallride.web.controller.guest.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

	@RequestMapping
	public String init() {
		return "user/password-reset";
	}
}
