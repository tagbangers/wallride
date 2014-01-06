package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="page")
@DynamicInsert
@DynamicUpdate
@Analyzer(definition="synonyms")
@Indexed
@SuppressWarnings("serial")
public class Page extends Post implements Comparable<Page>{

	@Column(name="lft", nullable=false)
	@Field
	private int lft;

	@Column(name="rgt", nullable=false)
	@Field
	private int rgt;

//	@Column(nullable=false)
//	@Field
//	private int depth;
//
//	@Column(nullable=false)
//	@Field
//	private int sort;

	@ManyToOne
//	@IndexedEmbedded //org.hibernate.search.SearchException: Circular reference.
	private Page parent;

	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
	private List<Page> children;

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

	//	public int getDepth() {
//		return depth;
//	}
//
//	public void setDepth(int depth) {
//		this.depth = depth;
//	}
//
//	public int getSort() {
//		return sort;
//	}
//
//	public void setSort(int sort) {
//		this.sort = sort;
//	}

	public Page getParent() {
		return parent;
	}

	public void setParent(Page parent) {
		this.parent = parent;
	}

	public List<Page> getChildren() {
		return children;
	}

	public void setChildren(List<Page> children) {
		this.children = children;
	}
	
	public int compareTo(Page page) {
		int lftDiff = getLft() - page.getLft();
		if (lftDiff != 0) {
			return lftDiff;
		}
		return (int) (page.getId() - getId());
	}
}
