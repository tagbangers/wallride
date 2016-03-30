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

package org.wallride.web.controller.guest.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.Article;
import org.wallride.domain.BlogLanguage;
import org.wallride.domain.Comment;
import org.wallride.domain.Post;
import org.wallride.model.ArticleSearchRequest;
import org.wallride.model.CommentSearchRequest;
import org.wallride.service.ArticleService;
import org.wallride.service.CommentService;
import org.wallride.web.support.HttpNotFoundException;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}/{code:.+}")
public class ArticleDescribeController {

	@Autowired
	private ArticleService articleService;

	@Autowired
	private CommentService commentService;

	@RequestMapping
	public String describe(
			@PathVariable int year,
			@PathVariable int month,
			@PathVariable int day,
			@PathVariable String code,
			BlogLanguage blogLanguage,
			Model model,
			RedirectAttributes redirectAttributes) {
		Article article = articleService.getArticleByCode(code, blogLanguage.getLanguage());
		if (article == null) {
			article = articleService.getArticleByCode(code, blogLanguage.getBlog().getDefaultLanguage());
		}
		if (article == null) {
			throw new HttpNotFoundException();
		}
		if (article.getStatus() != Post.Status.PUBLISHED) {
			throw new HttpNotFoundException();
		}

		LocalDate date = LocalDate.of(year, month, day);
		if (!article.getDate().toLocalDate().equals(date)) {
			redirectAttributes.addAttribute("year", article.getDate().getYear());
			redirectAttributes.addAttribute("month", article.getDate().getMonth().getValue());
			redirectAttributes.addAttribute("day", article.getDate().getDayOfMonth());
			redirectAttributes.addAttribute("code", code);
			return "redirect:/{year}/{month}/{day}/{code}";
		}

		CommentSearchRequest request = new CommentSearchRequest();
		request.setPostId(article.getId());
		request.setApproved(Boolean.TRUE);
		Page<Comment> comments = commentService.getComments(request, new PageRequest(0, 1000));

		List<Long> ids = articleService.getArticleIds(new ArticleSearchRequest().withStatus(Post.Status.PUBLISHED));
		if (!CollectionUtils.isEmpty(ids)) {
			int index = ids.indexOf(article.getId());
			if (index < ids.size() - 1) {
				Article next = articleService.getArticleById(ids.get(index + 1));
				model.addAttribute("next", next);
			}
			if (index > 0) {
				Article prev = articleService.getArticleById(ids.get(index - 1));
				model.addAttribute("prev", prev);
			}
		}
		model.addAttribute("article", article);
		model.addAttribute("comments", comments);
		return "article/describe";
	}
}
