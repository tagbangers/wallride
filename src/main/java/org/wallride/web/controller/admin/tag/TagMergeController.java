/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wallride.web.controller.admin.tag;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import javax.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.TagCreateRequest;
import org.wallride.core.service.TagDeleteRequest;
import org.wallride.core.service.TagService;
import org.wallride.core.support.AuthorizedUser;

/**
 *
 * @author hung132
 */
@Controller
@RequestMapping(value = "/{language}/tags/merge-tags", method = RequestMethod.POST)
public class TagMergeController {

    @Inject
    TagService tagService;

    @Inject
    ArticleService articleService;

    @RequestMapping(method = RequestMethod.POST)
    public String mergeTags(TagMergeForm form, RedirectAttributes redirect, Model model, AuthorizedUser authorizedUser) {
        model.addAttribute("ids", form.getIds());
        model.addAttribute("newtagname", form.getNewTagName());
        List<Long> tagsForMerging = form.getIds();
        TagCreateRequest requestCreate = new TagCreateRequest.Builder()
                .name(form.getNewTagName())
                .language(form.getLanguage())
                .build();
        
        // create a new merged tag
        Tag mergedTag = tagService.createTag(requestCreate, authorizedUser);
        // get all articles that have tag.
        List<Article> articles = tagService.getArticles(tagsForMerging);
        model.addAttribute("listArticle", articles);
        for (Article article : articles) {
            SortedSet<Tag> tags = article.getTags();
            Iterator<Tag> tagIterator = tags.iterator();
            while (tagIterator.hasNext()) {
                Tag tag = tagIterator.next();
                for (Long id : tagsForMerging) {
                    if (tag.getId() == id) {
                        tagIterator.remove();
                    }
                }
            }
            tags.add(mergedTag);
            articleService.updateArticleForTagMerging(article);
        }
        //delete  old tags after merging
        tagService.deleteTagsAfterMerging(form, null);
        return "redirect:/_admin/{language}/tags/index";
        // return "/tag/hung"; 
    }

}
