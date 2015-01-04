package org.wallride.core.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Field;

import javax.persistence.*;

@Entity
@Table(name = "blog_language", uniqueConstraints = @UniqueConstraint(columnNames = {"blog_id", "language"}))
@DynamicInsert
@DynamicUpdate
public class BlogLanguage extends DomainObject<Long> {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	private Blog blog;

	@Column(length = 3, nullable = false)
	@Field
	private String language;

	@Lob
	@Column(length = 300, nullable = false)
	@Field
	private String title;

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Blog getBlog() {
		return blog;
	}

	public void setBlog(Blog blog) {
		this.blog = blog;
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

	@Override
	public String toString() {
		return getBlog().getCode() + "-" + getLanguage();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof BlogLanguage)) return false;
		BlogLanguage that = (BlogLanguage) other;
		return new EqualsBuilder()
				.append(getId(), that.getId())
				.append(getLanguage(), that.getLanguage())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getId())
				.append(getLanguage())
				.toHashCode();
	}
}
