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
