package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Table(name = "tag")
@DynamicInsert
@DynamicUpdate
@Indexed
@SuppressWarnings("serial")
public class Tag extends DomainObject<Long> implements Comparable<Tag> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", length = 500, nullable = false)
	@Field
	private String name;

	@Column(length = 3, nullable = false)
	@Field
	private String language;

	@Formula("(" +
			"select count(distinct article.id) from article article " +
			"inner join post post on article.id = post.id " +
			"inner join article_tag tag on article.id = tag.article_id " +
			"where tag.tag_id = id " +
			"and post.status = 'PUBLISHED') ")
	private int articleCount;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getArticleCount() {
		return articleCount;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Tag tag) {
		return (int) (tag.getId() - getId());
	}
}
