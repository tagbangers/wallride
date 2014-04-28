package org.wallride.web.controller.admin.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.User;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.UserService;
import org.wallride.web.support.DomainObjectDescribeController;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping(value="/{language}/users/describe", method= RequestMethod.GET)
public class UserDescribeController extends DomainObjectDescribeController<User, UserSearchForm> {

	@Inject
	private UserService userService;

	@Inject
	private ArticleService articleService;

	@ModelAttribute("articleCounts")
	public Map<Long, Long> articleCounts(@PathVariable String language) {
		return articleService.countArticlesByAuthorIdGrouped(Post.Status.PUBLISHED, language);
	}

	@RequestMapping
	public String describe(
			@RequestParam long id,
			Model model,
			HttpSession session) {
		return super.requestMappingDescribe(id, null, model, session);
	}

	@RequestMapping(params = "pageable")
	public String describe(
			@RequestParam long id,
			@PageableDefault(50) Pageable pageable,
			Model model,
			HttpSession session) {
		return super.requestMappingDescribe(id, pageable, model, session);
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

	@Override
	protected Page<User> readDomainObjects(UserSearchForm form, Pageable pageable) {
		return userService.readUsers(form.buildUserSearchRequest(), pageable);
	}
}