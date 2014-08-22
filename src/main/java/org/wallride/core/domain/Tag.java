package org.wallride.core.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.SortedSet;
import java.util.TreeSet;

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

	@ManyToMany
	@JoinTable(
			name = "article_tag",
			joinColumns = {@JoinColumn(name = "tag_id")},
			inverseJoinColumns = @JoinColumn(name = "article_id", referencedColumnName = "id"))
	@SortNatural
	private SortedSet<Article> articles = new TreeSet<>();

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

	public SortedSet<Article> getArticles() {
		return articles;
	}

	public void setArticles(SortedSet<Article> articles) {
		this.articles = articles;
	}

	public int getArticleCount() {
		return articleCount;
	}

	@Field(analyze = Analyze.NO)
	public String getSortKey() {
		return getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Tag tag) {
		return new CompareToBuilder()
				.append(getName(), tag.getName())
				.append(getId(), tag.getId())
				.toComparison();
	}
}
