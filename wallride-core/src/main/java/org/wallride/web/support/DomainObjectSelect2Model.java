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

package org.wallride.web.support;

import org.wallride.core.domain.DomainObject;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DomainObjectSelect2Model<ID extends Serializable> implements Serializable {
	
	private ID id;
	private String text;
	
	public DomainObjectSelect2Model(DomainObject object) {
		setId((ID) object.getId());
		setText(object.toString());
	}
	
	public DomainObjectSelect2Model(ID id, String text) {
		setId(id);
		setText(text);
	}
	
	public ID getId() {
		return id;
	}
	
	public void setId(ID id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof DomainObjectSelect2Model)) return false;
		if (getId() == null) return false;
		DomainObjectSelect2Model that = (DomainObjectSelect2Model) other;
		return getId().equals(that.getId());
	}
}
