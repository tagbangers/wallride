package org.wallride.web.controller.admin.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/{language}/users/change-avatar", method=RequestMethod.POST)
public class UserChangeAvatar {
	
	
	public String changeAvatar(){
		return "redirect:/_admin/{language}/users/index";
	}

}
