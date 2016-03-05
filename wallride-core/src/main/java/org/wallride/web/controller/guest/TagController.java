package org.wallride.web.controller.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wallride.domain.BlogLanguage;
import org.wallride.domain.Post;
import org.wallride.domain.Tag;
import org.wallride.model.PostSearchRequest;
import org.wallride.model.TagSearchRequest;
import org.wallride.service.PostService;
import org.wallride.service.TagService;
import org.wallride.web.support.HttpNotFoundException;
import org.wallride.web.support.Pagination;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/tag")
public class TagController {

	@Inject
	private TagService tagService;
	@Inject
	private PostService postService;

	@RequestMapping
	public String index(
			@PageableDefault Pageable pageable,
			Model model,
			HttpServletRequest servletRequest) {
		Page<Tag> tags = tagService.getTags(new TagSearchRequest(), pageable);
		model.addAttribute("tags", tags);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(tags, servletRequest));
		return "tag/index";
	}

	@RequestMapping("/{name}")
	public String post(
			@PathVariable String name,
			@PageableDefault Pageable pageable,
			BlogLanguage blogLanguage,
			Model model,
			HttpServletRequest servletRequest) {
		Tag tag = tagService.getTagByName(name, blogLanguage.getLanguage());
		if (tag == null) {
			throw new HttpNotFoundException();
		}

		PostSearchRequest request = new PostSearchRequest(blogLanguage.getLanguage());
		request.withTagNames(name);

		Page<Post> posts = postService.getPosts(request, pageable);
		model.addAttribute("tag", tag);
		model.addAttribute("posts", posts);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(posts, servletRequest));
		return "tag/post";
	}
}
