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

package org.wallride.web.controller.admin.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.ServiceException;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collection;

@Controller
@RequestMapping(value="/{language}/articles/bulk-unpublish", method=RequestMethod.POST)
public class ArticleBulkUnpublishController {

	@Inject
	private ArticleService articleService;

	private static Logger logger = LoggerFactory.getLogger(ArticleBulkUnpublishController.class);

	@RequestMapping
	public String unpublish(
			@Valid @ModelAttribute("form") ArticleBulkUnpublishForm form,
			BindingResult errors,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes,
			Model model) {
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/articles/index";
		}

		Collection<Article> unpublishedArticles;
		try {
			unpublishedArticles = articleService.bulkUnpublishArticle(form.toArticleBulkUnpublishRequest(), authorizedUser);
		} catch (ServiceException e) {
			return "redirect:/_admin/{language}/articles/index";
		}

		redirectAttributes.addFlashAttribute("unpublishedArticles", unpublishedArticles);
		return "redirect:/_admin/{language}/articles/index";
	}
}
