package org.wallride.web.controller.admin.tag;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;

public class TagMergeForm implements Serializable {

    @NotNull
    private List<Long> ids;
    @NotNull
    private String newName;
    @NotNull
    private String language;

    public TagMergeForm(){}

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

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
    
    
}
