package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;

@Entity
@Table(name = "popular_post", uniqueConstraints = @UniqueConstraint(columnNames = {"language", "type", "rank"}))
@DynamicInsert
@DynamicUpdate
@Indexed
public class PopularPost extends DomainObject<Long> {

	public enum Type {
		DAILY,
		WEEKLY,
		MONTHLY,
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 3, nullable = false)
	@Field
	private String language;

	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	@Field
	private Type type;

	@Column(nullable = false)
	private int rank;

	@Column(nullable = false)
	private long views;

	@ManyToOne(optional = false)
	@IndexedEmbedded
	private Post post;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getViews() {
		return views;
	}

	public void setViews(long views) {
		this.views = views;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}
}
