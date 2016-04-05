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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Comment.SHALLOW_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("author")}
		),
		@NamedEntityGraph(name = Comment.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("author")})
})
@Table(name = "comment")
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
@SuppressWarnings("serial")
public class Comment extends DomainObject<Long> implements Comparable<Comment> {

	public static final String SHALLOW_GRAPH_NAME = "COMMENT_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "COMMENT_DEEP_GRAPH";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Field(name = "sortId", analyze = Analyze.NO, index = Index.NO)
	@SortableField(forField = "sortId")
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private User author;

	@Column(length = 200, nullable = false)
	@Field
	private String authorName;

	@Column(nullable = false)
	@Fields({
			@Field,
			@Field(name = "sortDate", analyze = Analyze.NO, index = org.hibernate.search.annotations.Index.NO)
	})
	@SortableField(forField = "sortDate")
	private LocalDateTime date;

	@Lob
	@Column(nullable = false)
	@Field
	private String content;

	@Column(nullable = false)
	@Field
	private boolean approved;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@Override
	public String print() {
		return this.getClass().getName() + " " + getId();
	}

	public int compareTo(Comment comment) {
		return new CompareToBuilder()
				.append(getDate(), comment.getDate())
				.append(getId(), comment.getId())
				.toComparison();
	}
}
