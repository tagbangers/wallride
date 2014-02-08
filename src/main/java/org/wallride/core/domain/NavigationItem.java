package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="navigation_item")
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type", discriminatorType=DiscriminatorType.STRING)
@DynamicInsert
@DynamicUpdate
@SuppressWarnings("serial")
public abstract class NavigationItem extends DomainObject<Long> {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(nullable=false)
	private int sort;

	@Column(nullable=false)
	private String language;

	@ManyToOne
	private NavigationItem parent;

	@OneToMany(mappedBy="parent")
	private Set<NavigationItem> children;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public NavigationItem getParent() {
		return parent;
	}

	public void setParent(NavigationItem parent) {
		this.parent = parent;
	}

	public Set<NavigationItem> getChildren() {
		return children;
	}

	public void setChildren(Set<NavigationItem> children) {
		this.children = children;
	}
}


