package org.wallride.web.controller.admin.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.Application;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleService;
import org.wallride.core.support.Pagination;
import org.wallride.web.support.DomainObjectSearchCondition;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/{language}/articles/index")
public class ArticleSearchController {

    @Inject
    private ArticleService articleService;

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

    @RequestMapping(method = RequestMethod.GET)
    public String search(
            @PathVariable String language,
            @Validated ArticleSearchForm form,
            BindingResult result,
            @PageableDefault(50) Pageable pageable,
            Model model,
            HttpSession session) {
        Page<Article> articles = articleService.readArticles(form.toArticleSearchRequest(), pageable);

        new DomainObjectSearchCondition<>(session, form, pageable);

        model.addAttribute("form", form);
        model.addAttribute("articles", articles);
        model.addAttribute("pageable", pageable);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(Application.ADMIN_SERVLET_PATH);
        builder.path("/{language}/articles/index");
        builder.queryParams(form.toQueryParams());
        String url = builder.buildAndExpand(language).toString();
        model.addAttribute("pagination", new Pagination<>(articles, url));
        return "article/index";
    }

//    @RequestMapping(params = "part=change-status-form")
//    public String partChangeStatusDialog() {
//        return "/article/index::change-status-form";
//    }
}
