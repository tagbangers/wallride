package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "comment")
@PrimaryKeyJoinColumn
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
@SuppressWarnings("serial")
public class Comment extends DomainObject<Long> {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	private User author;

	@Column(name = "author_name", length = 200, nullable = false)
	@Field
	private String authorName;

	@Column(nullable = false)
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
	@Field
	@FieldBridge(impl=LocalDateTimeBridge.class)
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
}
