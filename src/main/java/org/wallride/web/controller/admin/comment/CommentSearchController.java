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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.Application;
import org.wallride.core.domain.Comment;
import org.wallride.core.domain.Post;
import org.wallride.core.service.CommentService;
import org.wallride.core.service.CommentService;
import org.wallride.core.support.Pagination;
import org.wallride.web.controller.admin.comment.CommentSearchForm;
import org.wallride.web.support.DomainObjectSearchCondition;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/{language}/comments/index")
public class CommentSearchController {

    @Inject
    private CommentService commentService;

    @ModelAttribute("form")
    public CommentSearchForm setupCommentSearchForm() {
        return new CommentSearchForm();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String search(
            @PathVariable String language,
            @Validated CommentSearchForm form,
            BindingResult result,
            @PageableDefault(value = 50, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            HttpSession session) {
        Page<Comment> comments = commentService.readComments(form.toCommentSearchRequest(), pageable);

        new DomainObjectSearchCondition<>(session, form, pageable);

        model.addAttribute("form", form);
        model.addAttribute("comments", comments);
        model.addAttribute("pageable", pageable);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(Application.ADMIN_SERVLET_PATH);
        builder.path("/{language}/comments/index");
        builder.queryParams(form.toQueryParams());
        String url = builder.buildAndExpand(language).toString();
        model.addAttribute("pagination", new Pagination<>(comments, url));
        return "comment/index";
    }

    @RequestMapping(method = RequestMethod.GET, params = "part=bulk-delete-form")
    public String partBulkDeleteForm(@PathVariable String language) {
        return "comment/index::bulk-delete-form";
    }

    @RequestMapping(method = RequestMethod.GET, params = "part=bulk-approve-form")
    public String partBulkApproveForm(@PathVariable String language) {
        return "comment/index::bulk-approve-form";
    }

    @RequestMapping(method = RequestMethod.GET, params = "part=bulk-unapprove-form")
    public String partBulkUnapproveForm(@PathVariable String language) {
        return "comment/index::bulk-unapprove-form";
    }
}
