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

package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name="article")
@DynamicInsert
@DynamicUpdate
@Analyzer(definition="synonyms")
@Indexed
@SuppressWarnings("serial")
public class Article extends Post implements Comparable<Article> {

	@ManyToMany
	@JoinTable(
			name="article_category",
			joinColumns={@JoinColumn(name="article_id")},
			inverseJoinColumns=@JoinColumn(name="category_id", referencedColumnName="id"))
	@SortNatural
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private SortedSet<Category> categories = new TreeSet<>();


	public SortedSet<Category> getCategories() {
		return categories;
	}

	public void setCategories(SortedSet<Category> categories) {
		this.categories = categories;
	}

	public int compareTo(Article article) {
		if (getDate() != null && article.getDate() == null) return 1;
		if (getDate() == null && article.getDate() != null) return -1;
		if (getDate() != null && article.getDate() != null) {
			int r = getDate().compareTo(article.getDate());
			if (r != 0) return r * -1;
		}
		return (int) (article.getId() - getId());
	}
}
