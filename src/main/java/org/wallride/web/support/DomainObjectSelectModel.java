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
public class DomainObjectSelectModel<ID extends Serializable> implements Serializable {
	
	private ID id;
	private String text;
	
	public DomainObjectSelectModel(DomainObject object) {
		setId((ID) object.getId());
		setText(object.toString());
	}
	
	public DomainObjectSelectModel(ID id, String text) {
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
		if (other == null || !(other instanceof DomainObjectSelectModel)) return false;
		if (getId() == null) return false;
		DomainObjectSelectModel that = (DomainObjectSelectModel) other;
		return getId().equals(that.getId());
	}
}
