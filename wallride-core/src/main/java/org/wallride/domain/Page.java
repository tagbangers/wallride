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
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NamedEntityGraphs({
	@NamedEntityGraph(name = Page.SHALLOW_GRAPH_NAME,
			attributeNodes = {
					@NamedAttributeNode("cover"),
					@NamedAttributeNode("author"),
					@NamedAttributeNode("parent"),
					@NamedAttributeNode("children"),
					@NamedAttributeNode("categories"),
					@NamedAttributeNode("tags")
			}
	),
	@NamedEntityGraph(name = Page.DEEP_GRAPH_NAME,
			attributeNodes = {
					@NamedAttributeNode("cover"),
					@NamedAttributeNode("author"),
					@NamedAttributeNode("parent"),
					@NamedAttributeNode("children"),
					@NamedAttributeNode("categories"),
					@NamedAttributeNode("tags"),
					@NamedAttributeNode("relatedToPosts"),
					@NamedAttributeNode(value = "customFieldValues", subgraph = "customFieldValue")},
			subgraphs =  {
					@NamedSubgraph(name = "customFieldValue",
							attributeNodes = {
									@NamedAttributeNode("customField")})})
})
@Table(name="page")
@DynamicInsert
@DynamicUpdate
@Analyzer(definition="synonyms")
@Indexed
@SuppressWarnings("serial")
public class Page extends Post implements Comparable<Page> {

	public static final String SHALLOW_GRAPH_NAME = "PAGE_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "PAGE_DEEP_GRAPH";

	@Column(nullable=false)
	@Fields({
			@Field,
			@Field(name = "sortLft", analyze = Analyze.NO, index = org.hibernate.search.annotations.Index.NO)
	})
	@SortableField(forField = "sortLft")
	private int lft;

	@Column(nullable=false)
	@Field
	private int rgt;

	@ManyToOne
//	@IndexedEmbedded(includeEmbeddedObjectId = true) //org.hibernate.search.SearchException: Circular reference.
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
