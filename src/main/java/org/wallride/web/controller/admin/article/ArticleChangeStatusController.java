/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wallride.web.controller.admin.article;

import javax.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Post;
import org.wallride.core.service.ArticleChangeStatusRequest;
import org.wallride.core.service.ArticleService;

/**
 * http://localhost:8080/wallride/_admin/en/articles/change-status
 */
@Controller
@RequestMapping(value = "/{language}/articles/change-status", method = RequestMethod.POST)
public class ArticleChangeStatusController {

//    @Inject
//	private ArticleService articleService;
    @Inject
    private ArticleService articleService;

    @RequestMapping(method = RequestMethod.POST)
    public String changeStatus(ArticleChangeStatusForm form, Model model) {

        model.addAttribute("ids", form.getIds());
        model.addAttribute("form", form);
//        ArticleChangeStatusRequest request = form.buildArticleChangeStatusRequest();
//        
//        articleService.changeStatusAricle(request);

        Post.Status status = Post.Status.PUBLISHED;
        if (form.getStatus().equals("DRAFT")) {
            status = Post.Status.DRAFT;
        } else {
            if(form.getStatus().equals("SCHEDULED"))
            status = Post.Status.SCHEDULED;
        }

        articleService.changeAllStatus(status, form.getLanguage());
//        return "/article/hung";
        return "redirect:/_admin/{language}/articles/index";
    }
}
