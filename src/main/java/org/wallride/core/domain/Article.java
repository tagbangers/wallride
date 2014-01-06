package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name="article")
@PrimaryKeyJoinColumn
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
	@Sort(type=SortType.NATURAL)
	@IndexedEmbedded
	private SortedSet<Category> categories = new TreeSet<>();

	@ManyToMany
	@JoinTable(
			name="article_tag",
			joinColumns={@JoinColumn(name="article_id")},
			inverseJoinColumns=@JoinColumn(name="tag_id", referencedColumnName="id"))
	@Sort(type=SortType.NATURAL)
	@IndexedEmbedded
	private SortedSet<Tag> tags = new TreeSet<>();

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
