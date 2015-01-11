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

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Entity
@Table(name = "post", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicInsert
@DynamicUpdate
@Indexed
public class Post extends DomainObject<Long> {

	public enum Status {
		DRAFT, SCHEDULED, PUBLISHED
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(length=200)
	@Field
	private String code;

	@Column(length=3, nullable=false)
	@Field
	private String language;

	@Column(length=200)
	@Field
	private String title;

	@ManyToOne
	private Media cover;

	@Lob
	@Field
	private String body;

	@Embedded
	@IndexedEmbedded
	private Seo seo = new Seo();

	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
	@Field
	@FieldBridge(impl=LocalDateTimeBridge.class)
	private LocalDateTime date;

	@ManyToOne
	@IndexedEmbedded
	private User author;

	@Enumerated(EnumType.STRING)
	@Column(length=50, nullable=false)
	@Field
	private Status status;

	@Column(nullable = false)
	private long views;

	@ManyToOne
	@IndexedEmbedded(depth = 1, indexNullAs = Field.DEFAULT_NULL_TOKEN)
	private Post drafted;

	@Column(name = "drafted_code", length = 200)
	private String draftedCode;

	@OneToMany(mappedBy = "drafted", cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@SortNatural
	private SortedSet<Post> drafts;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@SortNatural
	private SortedSet<Comment> comments;

	@ManyToMany
	@JoinTable(
			name="post_related_post",
			joinColumns = { @JoinColumn(name="post_id")},
			inverseJoinColumns = { @JoinColumn(name="related_id") })
	private Set<Post> relatedToPosts = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name="post_related_post",
			joinColumns = { @JoinColumn(name="related_id")},
			inverseJoinColumns = { @JoinColumn(name="post_id") })
	private Set<Post> relatedByPosts = new HashSet<>();

	@ManyToMany
	@JoinTable(name="post_media", joinColumns=@JoinColumn(name="post_id", referencedColumnName="id"), inverseJoinColumns=@JoinColumn(name="media_id", referencedColumnName="id"))
	@OrderColumn(name="`index`")
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
		List<Post> relatedPostList = (List<Post>)CollectionUtils.union(getRelatedToPosts(), getRelatedByPosts());
		return new HashSet<Post>(relatedPostList);
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
