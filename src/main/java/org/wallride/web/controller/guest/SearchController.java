package org.wallride.web.controller.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Post;
import org.wallride.core.service.PostSearchRequest;
import org.wallride.core.service.PostService;
import org.wallride.core.support.Pagination;

import javax.inject.Inject;

@Controller
@RequestMapping("/search")
public class SearchController {

	@Inject
	private PostService postService;

	@RequestMapping
	public String search(
			@RequestParam String keyword,
			@PageableDefault(50) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		PostSearchRequest request = new PostSearchRequest(blogLanguage.getLanguage()).withKeyword(keyword);
		Page<Post> posts = postService.readPosts(request, pageable);
		model.addAttribute("keyword", keyword);
		model.addAttribute("posts", posts);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(posts));
		return "search";
	}
}
