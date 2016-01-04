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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Comment;
import org.wallride.core.model.CommentSearchRequest;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.CommentService;
import org.wallride.web.support.HttpNotFoundException;

import javax.inject.Inject;
import java.time.LocalDate;

@Controller
@RequestMapping("/{year:[0-9]{4}}/{month:[0-9]{2}}/{day:[0-9]{2}}/{code:.+}")
public class ArticleDescribeController {

	@Inject
	private ArticleService articleService;
	@Inject
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

		model.addAttribute("article", article);
		model.addAttribute("comments", comments);
		return "article/describe";
	}
}
