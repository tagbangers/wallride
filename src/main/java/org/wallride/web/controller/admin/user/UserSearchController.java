package org.wallride.web.controller.admin.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.User;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.UserService;
import org.wallride.web.support.DomainObjectSearchController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/{language}/users/index")
public class UserSearchController extends DomainObjectSearchController<User, UserSearchForm> {

	@Inject
	private UserService userService;

	@Inject
	private ArticleService articleService;

	@ModelAttribute("articleCounts")
	public Map<Long, Long> articleCounts(@PathVariable String language) {
		return articleService.countArticlesByAuthorIdGrouped(Post.Status.PUBLISHED, language);
	}

	@RequestMapping(method= RequestMethod.GET)
	public String index(
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session)
			throws Exception {
		return super.requestMappingIndex(model, request, response, session);
	}

	@RequestMapping(params="page")
	public String page(
			Pageable pageable,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		return super.requestMappingPage(pageable, model, request, response, session);
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
	protected Page<User> readDomainObjects(UserSearchForm form, Pageable pageable) {
		return userService.readUsers(form.buildUserSearchRequest(), pageable);
	}
}