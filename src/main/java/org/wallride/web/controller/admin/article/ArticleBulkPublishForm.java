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

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.wallride.core.service.ArticleBulkPublishRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class ArticleBulkPublishForm implements Serializable {

	private List<Long> ids;
	@DateTimeFormat(pattern="yyyy/MM/dd HH:mm")
	private LocalDateTime date;
	@NotNull
	private String language;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public ArticleBulkPublishRequest toArticleBulkPublishRequest() {
		ArticleBulkPublishRequest request = new ArticleBulkPublishRequest();
		request.setIds(getIds());
		request.setDate(getDate());
		request.setLanguage(getLanguage());
		return request;
	}
}
