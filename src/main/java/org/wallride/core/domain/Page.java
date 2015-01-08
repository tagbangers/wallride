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
public class Page extends Post implements Comparable<Page> {

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
