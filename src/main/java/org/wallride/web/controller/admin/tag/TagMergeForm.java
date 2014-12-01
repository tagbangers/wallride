/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wallride.web.controller.admin.tag;

import java.util.List;
import javax.validation.constraints.NotNull;

/**
 *
 * @author hung132
 */
public class TagMergeForm {

    @NotNull
    private List<Long> ids;
    @NotNull
    private String language;
    @NotNull
    private String newTagName;
   

    public TagMergeForm() {
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getNewTagName() {
        return newTagName;
    }

    public void setNewTagName(String newTagName) {
        this.newTagName = newTagName;
    }
    

}
