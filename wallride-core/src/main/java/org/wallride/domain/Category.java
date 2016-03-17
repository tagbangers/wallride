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
import org.hibernate.annotations.SortNatural;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Category.SHALLOW_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("parent"),
						@NamedAttributeNode("children")}
		),
		@NamedEntityGraph(name = Category.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("parent"),
						@NamedAttributeNode("children")})
})
@Table(name = "category", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@DynamicInsert
@DynamicUpdate
@Indexed
@SuppressWarnings("serial")
public class Category extends DomainObject<Long> implements Comparable<Category> {

	public static final String SHALLOW_GRAPH_NAME = "CATEGORY_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "CATEGORY_DEEP_GRAPH";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 200, nullable = false)
	@Field
	private String code;

	@Column(length = 3, nullable = false)
	@Field
	private String language;

	@Column(length = 200, nullable = false)
	@Field
	private String name;

	@Lob
	@Field
	private String description;

	@Column(nullable = false)
	@Field
	private int lft;

	@Column(nullable = false)
	@Field
	private int rgt;

	@ManyToOne
	private Category parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	private List<Category> children;

	@ManyToMany
	@JoinTable(
			name = "post_category",
			joinColumns = {@JoinColumn(name = "category_id")},
			inverseJoinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"))
	@SortNatural
	private SortedSet<Post> posts = new TreeSet<>();

//	@Formula("(" +
//			"select count(distinct article.id) from article article " +
//			"inner join post post on article.id = post.id " +
//			"inner join article_category category on article.id = category.article_id " +
//			"where category.category_id = id " +
//			"and post.status = 'PUBLISHED') ")
//	private int articleCount;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getLft() {
		return lft;
	}

	public void setLft(int lft) {
		this.lft = lft;
	}

	public int getRgt() {
		return rgt;
	}

	public void setRgt(int rgt) {
		this.rgt = rgt;
	}

	public Category getParent() {
		return parent;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}

	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		this.children = children;
	}

	public SortedSet<Post> getPosts() {
		return posts;
	}

	public void setPosts(SortedSet<Post> posts) {
		this.posts = posts;
	}

//	public int getArticleCount() {
//		return articleCount;
//	}

	@Override
	public String print() {
		return getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Category category) {
		int lftDiff = getLft() - category.getLft();
		if (lftDiff != 0) {
			return lftDiff;
		}
		return (int) (category.getId() - getId());
	}
}
