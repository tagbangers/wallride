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

package org.wallride.domain;

import org.hibernate.search.annotations.Field;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.io.Serializable;

@Embeddable
@SuppressWarnings("serial")
public class Seo implements Serializable {

	@Column(name = "seo_title", length = 500)
	@Field
	private String title;

	@Column(name = "seo_description")
	@Lob
	@Field
	private String description;

	@Column(name = "seo_keywords")
	@Lob
	@Field
	private String keywords;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
}
