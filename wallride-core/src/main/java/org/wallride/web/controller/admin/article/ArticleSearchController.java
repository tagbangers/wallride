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

import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleService;
import org.wallride.web.support.ControllerUtils;
import org.wallride.web.support.Pagination;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@Controller
@RequestMapping("/{language}/articles/index")
public class ArticleSearchController {

    @Inject
    private ArticleService articleService;

    @Inject
    private ConversionService conversionService;

    @ModelAttribute("countAll")
    public long countAll(@PathVariable String language) {
        return articleService.countArticles(language);
    }

    @ModelAttribute("countDraft")
    public long countDraft(@PathVariable String language) {
        return articleService.countArticlesByStatus(Post.Status.DRAFT, language);
    }

    @ModelAttribute("countScheduled")
    public long countScheduled(@PathVariable String language) {
        return articleService.countArticlesByStatus(Post.Status.SCHEDULED, language);
    }

    @ModelAttribute("countPublished")
    public long countPublished(@PathVariable String language) {
        return articleService.countArticlesByStatus(Post.Status.PUBLISHED, language);
    }

    @ModelAttribute("form")
    public ArticleSearchForm setupArticleSearchForm() {
        return new ArticleSearchForm();
    }

    @ModelAttribute("query")
    public String query(@RequestParam(required = false) String query) {
        return query;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String search(
            @PathVariable String language,
            @Validated @ModelAttribute("form") ArticleSearchForm form,
            BindingResult result,
            @PageableDefault(50) Pageable pageable,
            Model model,
            HttpServletRequest servletRequest) throws UnsupportedEncodingException {
        Page<Article> articles = articleService.getArticles(form.toArticleSearchRequest(), pageable);

        model.addAttribute("articles", articles);
        model.addAttribute("pageable", pageable);
        model.addAttribute("pagination", new Pagination<>(articles, servletRequest));

        UriComponents uriComponents = ServletUriComponentsBuilder
                .fromRequest(servletRequest)
                .queryParams(ControllerUtils.convertBeanForQueryParams(form, conversionService))
                .build();
        if (!StringUtils.isEmpty(uriComponents.getQuery())) {
            model.addAttribute("query", URLDecoder.decode(uriComponents.getQuery(), "UTF-8"));
        }

        return "article/index";
    }

    @RequestMapping(params = "query")
    public String search(
            @PathVariable String language,
            String query,
            Model model,
            SessionStatus sessionStatus,
            RedirectAttributes redirectAttributes) {
        sessionStatus.setComplete();

        for (Map.Entry<String, Object> mapEntry : model.asMap().entrySet()) {
            redirectAttributes.addFlashAttribute(mapEntry.getKey(), mapEntry.getValue());
        }
        String url = UriComponentsBuilder.fromPath("/_admin/{language}/articles/index")
                .query(query)
                .buildAndExpand(language)
                .encode()
                .toUriString();
        return "redirect:" + url;
    }

    @RequestMapping(method = RequestMethod.GET, params = "part=bulk-delete-form")
    public String partBulkDeleteForm(@PathVariable String language) {
        return "article/index::bulk-delete-form";
    }

    @RequestMapping(method = RequestMethod.GET, params = "part=bulk-publish-form")
    public String partBulkPublishForm(@PathVariable String language) {
        return "article/index::bulk-publish-form";
    }

    @RequestMapping(method = RequestMethod.GET, params = "part=bulk-unpublish-form")
    public String partBulkUnpublishForm(@PathVariable String language) {
        return "article/index::bulk-unpublish-form";
    }
}
