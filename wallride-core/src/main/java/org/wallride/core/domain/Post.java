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

import org.hibernate.annotations.*;
import org.hibernate.search.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Post.SHALLOW_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("cover"),
						@NamedAttributeNode("author")}
		),
		@NamedEntityGraph(name = Post.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("cover"),
						@NamedAttributeNode("author")})
})
@Table(name = "post", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicInsert
@DynamicUpdate
@Indexed
public class Post extends DomainObject<Long> {

	public static final String SHALLOW_GRAPH_NAME = "POST_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "POST_DEEP_GRAPH";

	public enum Status {
		DRAFT, SCHEDULED, PUBLISHED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 200)
	@Field
	private String code;

	@Column(length = 3, nullable = false)
	@Field
	private String language;

	@Column(length = 200)
	@Field
	private String title;

	@ManyToOne
	private Media cover;

	@Lob
	@Field
	private String body;

	@Embedded
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Seo seo = new Seo();

	@Field
	private LocalDateTime date;

	@ManyToOne
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private User author;

	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	@Field
	private Status status;

	@Column(nullable = false)
	private long views;

	@ManyToOne
	@IndexedEmbedded(includeEmbeddedObjectId = true, depth = 1, indexNullAs = Field.DEFAULT_NULL_TOKEN)
	private Post drafted;

	@Column(length = 200)
	private String draftedCode;

	@ManyToMany
	@JoinTable(
			name = "post_category",
			joinColumns = {@JoinColumn(name = "post_id")},
			inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id"))
	@SortNatural
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private SortedSet<Category> categories = new TreeSet<>();

	@ManyToMany
	@JoinTable(
			name = "post_tag",
			joinColumns = {@JoinColumn(name = "post_id")},
			inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
	@SortNatural
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private SortedSet<Tag> tags = new TreeSet<>();

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@SortNatural
	private List<CustomFieldValue> customFieldValues = new ArrayList<>();

	@OneToMany(mappedBy = "drafted", cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@SortNatural
	private SortedSet<Post> drafts;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@SortNatural
	@ContainedIn
	private SortedSet<Comment> comments;

	@ManyToMany
	@JoinTable(
			name = "post_related_post",
			joinColumns = {@JoinColumn(name = "post_id")},
			inverseJoinColumns = {@JoinColumn(name = "related_id")})
	private Set<Post> relatedToPosts = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "post_related_post",
			joinColumns = {@JoinColumn(name = "related_id")},
			inverseJoinColumns = {@JoinColumn(name = "post_id")})
	private Set<Post> relatedByPosts = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "post_media", joinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "media_id", referencedColumnName = "id"))
	@OrderColumn(name = "`index`")
	private List<Media> medias;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Media getCover() {
		return cover;
	}

	public void setCover(Media cover) {
		this.cover = cover;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Seo getSeo() {
		return seo;
	}

	public void setSeo(Seo seo) {
		this.seo = seo;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getViews() {
		return views;
	}

	public void setViews(long views) {
		this.views = views;
	}

	public SortedSet<Category> getCategories() {
		return categories;
	}

	public void setCategories(SortedSet<Category> categories) {
		this.categories = categories;
	}

	public SortedSet<Tag> getTags() {
		return tags;
	}

	public void setTags(SortedSet<Tag> tags) {
		this.tags = tags;
	}

	public List<CustomFieldValue> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(List<CustomFieldValue> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}

	public Post getDrafted() {
		return drafted;
	}

	public void setDrafted(Post drafted) {
		this.drafted = drafted;
	}

	public String getDraftedCode() {
		return draftedCode;
	}

	public void setDraftedCode(String draftedCode) {
		this.draftedCode = draftedCode;
	}

	public SortedSet<Post> getDrafts() {
		return drafts;
	}

	public void setDrafts(SortedSet<Post> drafts) {
		this.drafts = drafts;
	}

	public SortedSet<Comment> getComments() {
		return comments;
	}

	public void setComments(SortedSet<Comment> comments) {
		this.comments = comments;
	}

	public Set<Post> getRelatedPosts() {
		return getRelatedToPosts();
	}

	public Set<Post> getRelatedToPosts() {
		return relatedToPosts;
	}

	public void setRelatedToPosts(Set<Post> relatedToPosts) {
		this.relatedToPosts = relatedToPosts;
	}

	public Set<Post> getRelatedByPosts() {
		return relatedByPosts;
	}

	public void setRelatedByPosts(Set<Post> relatedByPosts) {
		this.relatedByPosts = relatedByPosts;
	}

	public List<Media> getMedias() {
		return medias;
	}

	public void setMedias(List<Media> medias) {
		this.medias = medias;
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
