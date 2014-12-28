package org.wallride.core.service;

import java.io.Serializable;
import java.util.List;

public class ArticleBulkUnpublishRequest implements Serializable {

	private List<Long> ids;
	private String language;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
