package org.wallride.web.controller.guest.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Post;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.PostService;
import org.wallride.core.service.TagService;
import org.wallride.core.support.Pagination;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PostIndexController {

	@Inject
	private TagService tagService;
	@Inject
	private PostService postService;

	@RequestMapping("/tag/{name}")
	public String tag(
			@PathVariable String name,
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model) {
		Tag tag = tagService.readTagByName(name, blogLanguage.getLanguage());
		if (tag == null) {
			throw new HttpNotFoundException();
		}

		List<String> tagNames = new ArrayList<>(1);
		tagNames.add(name);

		PostSearchForm form = new PostSearchForm() {};
		form.setTagNames(tagNames);

		Page<Post> posts = postService.readPosts(form.toPostSearchRequest(blogLanguage.getLanguage()), pageable);
		model.addAttribute("tag", tag);
		model.addAttribute("posts", posts);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(posts));
		return "post/index";
	}




}
