/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.web.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.*;
import org.wallride.core.service.*;
import org.wallride.web.controller.admin.article.ArticleSearchForm;

import javax.inject.Inject;
import java.util.List;
import java.util.SortedSet;

@Controller
public class DashboardController {

	@Inject
	private BlogService blogService;
	@Inject
	private PostService postService;
	@Inject
	private ArticleService articleService;
	@Inject
	private PageService pageService;
	@Inject
	private CategoryService categoryService;
	
	@RequestMapping({"/","/dashboard"})
	public String dashboard(RedirectAttributes redirectAttributes) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String defaultLanguage = blog.getDefaultLanguage();
		redirectAttributes.addAttribute("language", defaultLanguage);
		return "redirect:/_admin/{language}/";
	}
	
	@RequestMapping("/{language}/")
	public String dashboard(@PathVariable String language, Model model) {
		long articleCount = articleService.countArticlesByStatus(Post.Status.PUBLISHED, language);
		long pageCount = pageService.countPagesByStatus(Post.Status.PUBLISHED, language);

		CategoryTree categoryTreeHasArticle = categoryService.readCategoryTree(language, true);
		long categoryCount = categoryTreeHasArticle.getCategories().size();

		model.addAttribute("articleCount", articleCount);
		model.addAttribute("pageCount", pageCount);
		model.addAttribute("categoryCount", categoryCount);
		model.addAttribute("popularPosts", popularPosts(language));
		model.addAttribute("recentPublishedArticles", recentPublishedArticles(language));
		model.addAttribute("recentDraftArticles", recentDraftArticles(language));

		return "dashboard";
	}

	private SortedSet<PopularPost> popularPosts(String language) {
		return postService.readPopularPosts(language, PopularPost.Type.DAILY);
	}

	private List<Article> recentPublishedArticles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setStatus(Post.Status.PUBLISHED);
		Page<Article> page = articleService.readArticles(form.toArticleSearchRequest());
		return page.getContent();
	}

	private List<Article> recentDraftArticles(String language) {
		ArticleSearchForm form = new ArticleSearchForm();
		form.setStatus(Post.Status.DRAFT);
		Page<Article> page = articleService.readArticles(form.toArticleSearchRequest());
		return page.getContent();
	}
}
