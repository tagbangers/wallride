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
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Tag;
import org.wallride.core.service.ArticleService;
import org.wallride.core.service.TagCreateRequest;
import org.wallride.core.service.TagService;
import org.wallride.core.support.AuthorizedUser;

/**
 *
 * @author hung132
 */

@Controller
@RequestMapping(value= "/{language}/tags/merge-tags", method = RequestMethod.POST)
public class TagMergeController {
    
    @Inject 
    private TagService tagService;
    
    @Inject
    private ArticleService articleService;
    
    @RequestMapping(method = RequestMethod.POST )
    public String mergeTags(@Valid TagMergeForm form, BindingResult errors, RedirectAttributes redirect, AuthorizedUser authorizeUser){
        if (errors.hasErrors()) {
            return "redirect:/_admin/{language}/tags/index";
        }
        List<Long> tagsForMerging = form.getIds();
        
        TagCreateRequest requestCreate  = new TagCreateRequest.Builder()
                .name(form.getNewTagName())
                .language(form.getLanguage())
                .build();
        // create a new merged tag;
        Tag newMergedTag = tagService.createTag(requestCreate, authorizeUser);
        //get all articles that have tag for merging;
        List<Article> articles  = tagService.getArticles(tagsForMerging);
        
        for (Article article : articles) {
            SortedSet<Tag> tags = article.getTags();
            Iterator<Tag> tagIterator = tags.iterator();
            while(tagIterator.hasNext()){
                Tag next = tagIterator.next();
                for (Long tagId : tagsForMerging) {
                    if(next.getId() == tagId){
                        tagIterator.remove();
                    }
                }
            }
            tags.add(newMergedTag);
            articleService.updateArticleForTagMerging(article);
        }
        // delete old tag after mergin
        
        tagService.deleteTagsAfterMerging(form, errors);
        redirect.addFlashAttribute("mergedTags", articles);
        return "redirect:/_admin/{language}/tags/index";
    }
}
