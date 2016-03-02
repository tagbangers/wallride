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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Article.SHALLOW_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("cover"),
						@NamedAttributeNode("author"),
						@NamedAttributeNode("drafted"),
						@NamedAttributeNode("categories")}
		),
		@NamedEntityGraph(name = Article.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("cover"),
						@NamedAttributeNode("author"),
						@NamedAttributeNode("drafted"),
						@NamedAttributeNode("categories"),
						@NamedAttributeNode("tags"),
						@NamedAttributeNode("relatedToPosts")})
})
@Table(name = "article")
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
@SuppressWarnings("serial")
public class Article extends Post implements Comparable<Article> {

	public static final String SHALLOW_GRAPH_NAME = "ARTICLE_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "ARTICLE_DEEP_GRAPH";

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
