package org.wallride.web.admin.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.service.UserService;
import org.wallride.domain.User;
import org.wallride.support.Paginator;
import org.wallride.web.DomainObjectSearchController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Controller @Lazy
@RequestMapping("/{language}/users/index")
public class UserSearchController extends DomainObjectSearchController<User, UserSearchForm> {
	
	@Inject
	private UserService userService;

	@RequestMapping(method= RequestMethod.GET)
	public String index(
			@RequestParam(required=false) String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session)
			throws Exception {
		return super.requestMappingIndex(token, model, request, response, session);
	}
	
	@RequestMapping(params="page")
	public String page(
			@RequestParam int page,
			@RequestParam(required=false) String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		return super.requestMappingPage(page, token, model, request, response, session);
	}
	
	@RequestMapping(params="search")
	public String search(
			@Valid UserSearchForm form,
			BindingResult result,
			Model model,
			HttpSession session,
			RedirectAttributes redirectAttributes) {
		return super.requestMappingSearch(form, result, model, session, redirectAttributes);
	}

	@RequestMapping(params="part=bulk-delete-dialog")
	public String partBulkDeleteDialog() {
		return "/user/index::#bulk-delete-dialog";
	}

	@Override
	protected Class<UserSearchForm> getDomainObjectSearchFormClass() {
		return UserSearchForm.class;
	}

	@Override
	protected String getModelAttributeName() {
		return "users";
	}

	@Override
	protected String getViewName() {
		return "/user/index";
	}

	@Override
	protected String getRedirectViewName() {
		return "redirect:/_admin/{language}/users/index";
	}

	@Override
	protected Paginator<Long> readDomainObjects(UserSearchForm form, int perPage) {
		List<Long> ids = userService.searchUsers(form.buildUserSearchRequest());
		return new Paginator<>(ids, perPage);
	}

	@Override
	protected Collection<User> readDomainObjects(Paginator<Long> paginator) {
		return userService.readUsers(paginator);
	}
}