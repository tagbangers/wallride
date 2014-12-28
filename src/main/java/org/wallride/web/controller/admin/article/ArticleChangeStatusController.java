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
import org.wallride.core.service.ArticleService;

/**
 * 
 */
@Controller
@RequestMapping(value = "/{language}/articles/change-status", method = RequestMethod.POST)
public class ArticleChangeStatusController {

    @Inject
    private ArticleService articleService;

    @RequestMapping(method = RequestMethod.POST)
    public String changeStatus(ArticleChangeStatusForm form, RedirectAttributes redirect) {

        Post.Status status = Post.Status.PUBLISHED;
        if (form.getStatus().equals("DRAFT")) {
            status = Post.Status.DRAFT;
        } else {
            if(form.getStatus().equals("SCHEDULED"))
            status = Post.Status.SCHEDULED;
        }
        articleService.changeStatusArticle(status, form.getLanguage(), form.getIds());
        redirect.addFlashAttribute("changedStatus", "success");
        return "redirect:/_admin/{language}/articles/index";
    }
}
