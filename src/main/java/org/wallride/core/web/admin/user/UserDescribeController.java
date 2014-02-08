package org.wallride.core.web.admin.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.service.UserService;
import org.wallride.core.domain.User;
import org.wallride.core.web.DomainObjectDescribeController;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@Controller @Lazy
@RequestMapping(value="/{language}/users/describe", method= RequestMethod.GET)
public class UserDescribeController extends DomainObjectDescribeController<User, UserSearchForm> {
	
	@Inject
	private UserService userService;
	
	@RequestMapping
	public String describe( 
			@RequestParam long id,
			@RequestParam(required=false) String token,
			Model model,
			HttpSession session) {
		return super.requestMappingDescribe(id, token, model, session);
	}

	@Override
	protected Class<UserSearchForm> getDomainObjectSearchFormClass() {
		return UserSearchForm.class;
	}

	@Override
	protected String getModelAttributeName() {
		return "user";
	}

	@Override
	protected String getViewName() {
		return "/user/describe";
	}

	@Override
	protected User readDomainObject(long id) {
		return userService.readUserById(id);
	}
}