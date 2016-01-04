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

package org.wallride.web.controller.admin.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Comment;
import org.wallride.core.exception.ServiceException;
import org.wallride.core.service.CommentService;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collection;

@Controller
@RequestMapping(value="/{language}/comments/bulk-approve", method=RequestMethod.POST)
public class CommentBulkApproveController {

	@Inject
	private CommentService commentService;
	
	private static Logger logger = LoggerFactory.getLogger(CommentBulkApproveController.class);

	@ModelAttribute("query")
	public String query(@RequestParam(required = false) String query) {
		return query;
	}

	@RequestMapping
	public String approve(
			@Valid @ModelAttribute("form") CommentBulkApproveForm form,
			BindingResult errors,
			String query,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes,
			Model model) {
		redirectAttributes.addAttribute("query", query);

		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/comments/index";
		}
		
		Collection<Comment> approvedComments;
		try {
			approvedComments = commentService.bulkApproveComment(form.toCommentBulkApproveRequest(), authorizedUser);
		} catch (ServiceException e) {
			return "redirect:/_admin/{language}/comments/index";
		}

		redirectAttributes.addFlashAttribute("approvedComments", approvedComments);
		return "redirect:/_admin/{language}/comments/index";
	}
}
