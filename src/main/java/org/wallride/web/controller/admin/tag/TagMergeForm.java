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

package org.wallride.web.controller.admin.tag;

import org.wallride.core.model.TagMergeRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class TagMergeForm implements Serializable {

    @NotNull
    private List<Long> ids;
    @NotNull
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TagMergeRequest toTagMergeRequest() {
        TagMergeRequest request = new TagMergeRequest();
        request.setIds(getIds());
        request.setName(getName());
        request.setLanguage(getLanguage());
        return request;
    }
}
